package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.model.FeedstockMeasurementSugarAcidityPagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.FeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.CurrentFeedstockBatchSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockProfileAxisEvidence;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementProfileView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.core.measurement.application.port.in.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.core.measurement.domain.projection.MeasurementChemicalProfileProjector;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement.MeasurementJdbcReadSupport;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/**
 * Feedstock read adapter over generic measurement evidence.
 *
 * <p>Historical queries project one active {@code MeasurementSet}. Current queries follow the
 * material-state lineage and select the nearest applicable RESULT independently for each
 * characteristic, so a partial measurement does not hide inherited evidence.
 */
@Repository
@RequiredArgsConstructor
class JdbcFeedstockMeasurementQueryRepository
    implements FeedstockMeasurementQueryRepository {

  private static final Set<MeasurementCharacteristic> CHEMICAL_CHARACTERISTICS = Set.of(
      MeasurementCharacteristic.SPECIFIC_GRAVITY,
      MeasurementCharacteristic.PH,
      MeasurementCharacteristic.TITRATABLE_ACIDITY,
      MeasurementCharacteristic.TANNIN,
      MeasurementCharacteristic.YAN
  );

  private static final String SET_CANDIDATES = """
      SELECT s.measurement_set_id,
             s.workspace_id,
             s.subject_id AS batch_id,
             revision.name,
             revision.note,
             revision.material_state_id,
             s.version,
             MAX(record.measured_at) AS measured_at
        FROM measurement_sets
        JOIN measurement_set_revisions revision
          ON revision.revision_id = s.current_revision_id
        JOIN measurement_revision_records revision_record
          ON revision_record.revision_id = revision.revision_id
        JOIN measurement_records record
          ON record.record_id = revision_record.record_id
         AND record.role = 'RESULT'
        JOIN feedstock_batches batch
          ON batch.batch_id = s.subject_id
         AND batch.deleted_at IS NULL
       WHERE s.subject_type = 'FEEDSTOCK_BATCH'
         AND s.lifecycle = 'ACTIVE'
         AND record.characteristic IN (
             'SPECIFIC_GRAVITY',
             'PH',
             'TITRATABLE_ACIDITY',
             'TANNIN',
             'YAN'
         )
      """;

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;
  private final @NonNull ProjectCurrentMeasurementProfileUseCase currentProfileProjector;

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurementView> findById(
      @NonNull FeedstockMeasurementId measurementId
  ) {
    requireNonNull(measurementId, "feedstock measurement id cannot be null");
    final var candidates = jdbcTemplate.query(
        SET_CANDIDATES
            + """
               AND s.measurement_set_id = :measurementSetId
               GROUP BY s.measurement_set_id,
                        s.workspace_id,
                        s.subject_id,
                        revision.name,
                        revision.note,
                        revision.material_state_id,
                        s.version
              """,
        new MapSqlParameterSource("measurementSetId", measurementId.value()),
        JdbcFeedstockMeasurementQueryRepository::mapCandidate
    );
    return loadSetViews(candidates).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementView> findSugarAcidityPointCandidates(
      @NonNull FeedstockMeasurementSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "feedstock measurement point candidate query cannot be null");
    final var candidates = querySetCandidates(
        query.workspaceId(),
        query.batch(),
        query.season(),
        query.after(),
        CursorDirection.ASCENDING,
        query.limit()
    );
    return loadSetViews(candidates);
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementView> findCurrentSugarAcidityPointCandidates(
      @NonNull CurrentFeedstockBatchSugarAcidityPointCandidateQuery query
  ) {
    requireNonNull(query, "current feedstock batch point candidate query cannot be null");
    final var batchIds = queryCurrentBatchIds(
        query.workspaceId(),
        query.batch(),
        query.season(),
        query.after(),
        CursorDirection.DESCENDING,
        query.limit()
    );
    return loadCurrentViews(batchIds);
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockMeasurementView> findCurrentByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return loadCurrentViews(List.of(batchId)).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementView> findCurrentByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    return loadCurrentViews(List.copyOf(batchIds));
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    final var candidates = querySetCandidates(
        null,
        FeedstockBatchCriteria.of(batchId),
        null,
        null,
        CursorDirection.DESCENDING,
        Integer.MAX_VALUE
    );
    return loadSetViews(candidates).stream()
        .map(view -> new FeedstockMeasurementSummary(
            view.measurementId(),
            workspaceId(candidates, view.measurementId()),
            view.batchId(),
            view.name(),
            view.measuredAt(),
            view.chemicalProfile(),
            view.note()
        ))
        .toList();
  }

  private @NonNull List<@NonNull SetCandidate> querySetCandidates(
      @Nullable WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season,
      @Nullable FeedstockMeasurementSugarAcidityPagePosition cursor,
      @NonNull CursorDirection direction,
      int limit
  ) {
    final var sql = new StringBuilder(SET_CANDIDATES);
    final var parameters = new MapSqlParameterSource();
    addFilters(sql, parameters, workspaceId, batchCriteria, season);
    sql.append("""
         GROUP BY s.measurement_set_id,
                  s.workspace_id,
                  s.subject_id,
                  revision.name,
                  revision.note,
                  revision.material_state_id,
                  s.version
        """);
    if (cursor != null) {
      sql.append("""
           HAVING MAX(record.measured_at) %s :afterMeasuredAt
               OR (
                   MAX(record.measured_at) = :afterMeasuredAt
                   AND s.measurement_set_id %s :afterMeasurementSetId
               )
          """.formatted(direction.operator(), direction.operator()));
      parameters
          .addValue("afterMeasuredAt", cursor.measuredAt())
          .addValue("afterMeasurementSetId", cursor.measurementId());
    }
    sql.append(" ORDER BY measured_at ")
        .append(direction.order())
        .append(", s.measurement_set_id ")
        .append(direction.order());
    if (limit != Integer.MAX_VALUE) {
      sql.append(" LIMIT :limit");
      parameters.addValue("limit", limit);
    }
    return jdbcTemplate.query(sql.toString(), parameters, JdbcFeedstockMeasurementQueryRepository::mapCandidate);
  }

  private @NonNull List<@NonNull FeedstockBatchId> queryCurrentBatchIds(
      @NonNull WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season,
      @Nullable FeedstockMeasurementSugarAcidityPagePosition cursor,
      @NonNull CursorDirection direction,
      int limit
  ) {
    final var sql = new StringBuilder("""
        WITH RECURSIVE state_lineage AS (
            SELECT 'FEEDSTOCK_BATCH' AS subject_type,
                   batch.batch_id AS subject_id,
                   batch.current_material_state_id AS material_state_id,
                   0 AS state_depth,
                   ARRAY[batch.current_material_state_id] AS visited_states,
                   ARRAY[]::VARCHAR[] AS invalidated_characteristics
              FROM feedstock_batches batch
             WHERE batch.workspace_id = :workspaceId

            UNION ALL

            SELECT lineage.subject_type,
                   lineage.subject_id,
                   input_state.material_state_id,
                   lineage.state_depth + 1,
                   lineage.visited_states || input_state.material_state_id,
                   lineage.invalidated_characteristics || output_state.invalidated_characteristics
              FROM state_lineage lineage
              JOIN batch_trace_step_subjects output_state
                ON output_state.material_state_id = lineage.material_state_id
               AND output_state.subject_role = 'OUTPUT'
               AND output_state.subject_type = lineage.subject_type
               AND output_state.subject_id = lineage.subject_id
              JOIN batch_trace_step_subjects input_state
                ON input_state.step_id = output_state.step_id
               AND input_state.subject_role = 'INPUT'
               AND input_state.subject_type = lineage.subject_type
               AND input_state.subject_id = lineage.subject_id
             WHERE NOT input_state.material_state_id = ANY(lineage.visited_states)
        ),
        distinct_lineage AS (
            SELECT subject_type,
                   subject_id,
                   material_state_id,
                   MIN(state_depth) AS state_depth,
                   invalidated_characteristics
              FROM state_lineage
             GROUP BY subject_type,
                      subject_id,
                      material_state_id,
                      invalidated_characteristics
        ),
        ranked_records AS (
            SELECT s.subject_id AS batch_id,
                   s.measurement_set_id,
                   record.record_id,
                   record.characteristic,
                   record.measured_at,
                   ROW_NUMBER() OVER (
                       PARTITION BY s.subject_id, record.characteristic
                       ORDER BY lineage.state_depth,
                                record.measured_at DESC,
                                record.record_id DESC,
                                s.measurement_set_id DESC
                   ) AS characteristic_rank
              FROM distinct_lineage lineage
              JOIN measurement_sets s
                ON s.subject_type = lineage.subject_type
               AND s.subject_id = lineage.subject_id
               AND s.lifecycle = 'ACTIVE'
              JOIN measurement_set_revisions revision
                ON revision.revision_id = s.current_revision_id
               AND revision.material_state_id = lineage.material_state_id
              JOIN measurement_revision_records revision_record
                ON revision_record.revision_id = revision.revision_id
              JOIN measurement_records record
                ON record.record_id = revision_record.record_id
               AND record.role = 'RESULT'
               AND NOT record.characteristic = ANY(lineage.invalidated_characteristics)
              JOIN feedstock_batches batch
                ON batch.batch_id = s.subject_id
               AND batch.deleted_at IS NULL
             WHERE record.characteristic IN (
                   'SPECIFIC_GRAVITY',
                   'PH',
                   'TITRATABLE_ACIDITY',
                   'TANNIN',
                   'YAN'
               )
        """);
    final var parameters = new MapSqlParameterSource();
    addFilters(sql, parameters, workspaceId, batchCriteria, season);
    sql.append("""
        ),
        current_records AS (
            SELECT *
              FROM ranked_records
             WHERE characteristic_rank = 1
        ),
        current_batches AS (
            SELECT batch_id,
                   MAX(measured_at) AS measured_at,
                   (ARRAY_AGG(
                       measurement_set_id
                       ORDER BY measured_at DESC, record_id DESC, measurement_set_id DESC
                   ))[1] AS anchor_set_id
              FROM current_records
             GROUP BY batch_id
        )
        SELECT batch_id
          FROM current_batches
         WHERE 1 = 1
        """);
    if (cursor != null) {
      sql.append("""
           AND (
               measured_at %s :afterMeasuredAt
               OR (
                   measured_at = :afterMeasuredAt
                   AND anchor_set_id %s :afterMeasurementSetId
               )
           )
          """.formatted(direction.operator(), direction.operator()));
      parameters
          .addValue("afterMeasuredAt", cursor.measuredAt())
          .addValue("afterMeasurementSetId", cursor.measurementId());
    }
    sql.append(" ORDER BY measured_at ")
        .append(direction.order())
        .append(", anchor_set_id ")
        .append(direction.order())
        .append(" LIMIT :limit");
    parameters.addValue("limit", limit);
    return jdbcTemplate.query(
        sql.toString(),
        parameters,
        (resultSet, rowNumber) -> FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class))
    );
  }

  private static void addFilters(
      @NonNull StringBuilder sql,
      @NonNull MapSqlParameterSource parameters,
      @Nullable WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season
  ) {
    if (workspaceId != null) {
      sql.append(" AND s.workspace_id = :workspaceId");
      parameters.addValue("workspaceId", workspaceId.value());
    }
    if (batchCriteria != null) {
      sql.append(" AND s.subject_id IN (:batchIds)");
      parameters.addValue(
          "batchIds",
          batchCriteria.batchIds().stream().map(FeedstockBatchId::value).toList()
      );
    }
    if (season != null) {
      sql.append("""
           AND batch.season_starts_on <= :seasonEndsOn
           AND batch.season_ends_on >= :seasonStartsOn
          """);
      parameters
          .addValue("seasonStartsOn", season.startsOn())
          .addValue("seasonEndsOn", season.endsOn());
    }
  }

  private @NonNull List<@NonNull FeedstockMeasurementView> loadSetViews(
      @NonNull List<@NonNull SetCandidate> candidates
  ) {
    if (candidates.isEmpty()) {
      return List.of();
    }
    final var records = loadRecordsBySetIds(candidates.stream().map(SetCandidate::measurementSetId).toList());
    return candidates.stream()
        .map(candidate -> toView(candidate, records.getOrDefault(candidate.measurementSetId(), List.of())))
        .flatMap(Optional::stream)
        .toList();
  }

  private @NonNull List<@NonNull FeedstockMeasurementView> loadCurrentViews(
      @NonNull List<@NonNull FeedstockBatchId> batchIds
  ) {
    if (batchIds.isEmpty()) {
      return List.of();
    }
    final var workspacesByBatch = loadWorkspaces(batchIds);
    final Map<FeedstockBatchId, CurrentMeasurementProfileView> profilesByBatch = new LinkedHashMap<>();
    workspacesByBatch.entrySet().stream()
        .collect(Collectors.groupingBy(Map.Entry::getValue))
        .forEach((workspaceId, batchEntries) -> {
          final var subjects = batchEntries.stream()
              .map(Map.Entry::getKey)
              .map(JdbcFeedstockMeasurementQueryRepository::subject)
              .collect(Collectors.toUnmodifiableSet());
          currentProfileProjector.projectAll(workspaceId, subjects)
              .forEach((subject, profile) -> profilesByBatch.put(
                  FeedstockBatchId.of(subject.id()),
                  profile
              ));
        });

    final var metadataBySet = loadSetMetadata(profilesByBatch.values().stream()
        .flatMap(profile -> profile.evidence().stream())
        .map(MeasurementEvidenceView::evidenceReference)
        .map(evidenceReference -> evidenceReference.measurementSetId())
        .collect(Collectors.toUnmodifiableSet()));

    return batchIds.stream()
        .map(batchId -> toCurrentView(batchId, profilesByBatch.get(batchId), metadataBySet))
        .flatMap(Optional::stream)
        .toList();
  }

  private @NonNull Map<@NonNull FeedstockBatchId, @NonNull WorkspaceId> loadWorkspaces(
      @NonNull List<@NonNull FeedstockBatchId> batchIds
  ) {
    return jdbcTemplate.query(
            """
                SELECT batch_id,
                       workspace_id
                  FROM feedstock_batches
                 WHERE batch_id IN (:batchIds)
                   AND deleted_at IS NULL
                """,
            new MapSqlParameterSource(
                "batchIds",
                batchIds.stream().map(FeedstockBatchId::value).toList()
            ),
            (resultSet, rowNumber) -> Map.entry(
                FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
                WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class))
            )
        )
        .stream()
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private @NonNull Map<@NonNull UUID, @NonNull SetMetadata> loadSetMetadata(
      @NonNull Set<@NonNull UUID> measurementSetIds
  ) {
    if (measurementSetIds.isEmpty()) {
      return Map.of();
    }
    return jdbcTemplate.query(
            """
                SELECT measurement_set.measurement_set_id,
                       measurement_set.version,
                       revision.name,
                       revision.note
                  FROM measurement_sets measurement_set
                  JOIN measurement_set_revisions revision
                    ON revision.revision_id = measurement_set.current_revision_id
                 WHERE measurement_set.measurement_set_id IN (:measurementSetIds)
                """,
            new MapSqlParameterSource("measurementSetIds", measurementSetIds),
            (resultSet, rowNumber) -> new SetMetadata(
                resultSet.getObject("measurement_set_id", UUID.class),
                resultSet.getString("name"),
                resultSet.getString("note"),
                resultSet.getLong("version")
            )
        )
        .stream()
        .collect(Collectors.toUnmodifiableMap(SetMetadata::measurementSetId, value -> value));
  }

  private @NonNull Map<UUID, List<RecordRow>> loadRecordsBySetIds(
      @NonNull List<@NonNull UUID> measurementSetIds
  ) {
    final var rows = jdbcTemplate.query(
        """
            SELECT s.measurement_set_id,
                   s.subject_id AS batch_id,
                   revision.material_state_id,
                   revision.name,
                   revision.note,
                   s.version,
                   record.*
              FROM measurement_sets s
              JOIN measurement_set_revisions revision
                ON revision.revision_id = s.current_revision_id
              JOIN measurement_revision_records revision_record
                ON revision_record.revision_id = revision.revision_id
              JOIN measurement_records record
                ON record.record_id = revision_record.record_id
               AND record.role = 'RESULT'
             WHERE s.measurement_set_id IN (:measurementSetIds)
               AND record.characteristic IN (
                   'SPECIFIC_GRAVITY',
                   'PH',
                   'TITRATABLE_ACIDITY',
                   'TANNIN',
                   'YAN'
               )
             ORDER BY s.measurement_set_id, record.characteristic
            """,
        new MapSqlParameterSource("measurementSetIds", measurementSetIds),
        JdbcFeedstockMeasurementQueryRepository::mapRecord
    );
    final Map<UUID, List<RecordRow>> result = new LinkedHashMap<>();
    rows.forEach(row -> result.computeIfAbsent(row.measurementSetId(), _ -> new ArrayList<>()).add(row));
    return result;
  }

  private static @NonNull Optional<@NonNull FeedstockMeasurementView> toView(
      @NonNull SetCandidate candidate,
      @NonNull List<RecordRow> records
  ) {
    final var profile = toChemicalProfile(records);
    return profile.map(value -> new FeedstockMeasurementView(
        FeedstockMeasurementId.of(candidate.measurementSetId()),
        candidate.batchId(),
        candidate.materialStateId(),
        FeedstockMeasurementName.of(candidate.name()),
        value,
        candidate.measuredAt(),
        candidate.version(),
        toEvidence(records),
        candidate.note()
    ));
  }

  private static @NonNull Optional<@NonNull FeedstockMeasurementView> toCurrentView(
      @NonNull FeedstockBatchId batchId,
      @Nullable CurrentMeasurementProfileView profile,
      @NonNull Map<@NonNull UUID, @NonNull SetMetadata> metadataBySet
  ) {
    if (profile == null) {
      return Optional.empty();
    }
    final var anchor = profile.evidence().stream()
        .max(Comparator
            .comparing(MeasurementEvidenceView::measuredAt)
            .thenComparing(evidence -> evidence.evidenceReference().recordId())
            .thenComparing(evidence -> evidence.evidenceReference().measurementSetId()))
        .orElseThrow();
    final var anchorReference = anchor.evidenceReference();
    final var metadata = requireNonNull(
        metadataBySet.get(anchorReference.measurementSetId()),
        "measurement set metadata cannot be null"
    );
    return Optional.of(new FeedstockMeasurementView(
        FeedstockMeasurementId.of(anchorReference.measurementSetId()),
        batchId,
        profile.currentMaterialStateId(),
        FeedstockMeasurementName.of(metadata.name()),
        profile.chemicalProfile(),
        anchor.measuredAt(),
        metadata.version(),
        toCurrentEvidence(profile.evidence()),
        metadata.note()
    ));
  }

  private static @NonNull Optional<@NonNull ChemicalProfile> toChemicalProfile(
      @NonNull List<RecordRow> records
  ) {
    final var byCharacteristic =
        new EnumMap<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>>(
            MeasurementCharacteristic.class
        );
    records.stream()
        .filter(recordRow -> CHEMICAL_CHARACTERISTICS.contains(recordRow.characteristic()))
        .forEach(recordRow -> byCharacteristic.put(recordRow.characteristic(), recordRow.value()));
    return MeasurementChemicalProfileProjector.project(byCharacteristic);
  }

  private static @NonNull List<@NonNull FeedstockProfileAxisEvidence> toEvidence(
      @NonNull List<RecordRow> records
  ) {
    return records.stream()
        .filter(recordRow -> CHEMICAL_CHARACTERISTICS.contains(recordRow.characteristic()))
        .map(recordRow -> new FeedstockProfileAxisEvidence(
            recordRow.characteristic(),
            MeasurementValueOrigin.MEASURED,
            recordRow.materialStateId(),
            recordRow.measurementSetId(),
            recordRow.recordId(),
            recordRow.measuredAt(),
            recordRow.source(),
            recordRow.confidence(),
            MeasurementEvidenceValidity.VALID
        ))
        .toList();
  }

  private static @NonNull List<@NonNull FeedstockProfileAxisEvidence> toCurrentEvidence(
      @NonNull List<@NonNull MeasurementEvidenceView> evidence
  ) {
    return evidence.stream()
        .filter(item -> CHEMICAL_CHARACTERISTICS.contains(item.characteristic()))
        .map(item -> new FeedstockProfileAxisEvidence(
            item.characteristic(),
            item.origin(),
            item.materialStateId(),
            item.evidenceReference().measurementSetId(),
            item.evidenceReference().recordId(),
            item.measuredAt(),
            item.provenance().sourceName(),
            item.provenance().confidence(),
            item.validity()
        ))
        .toList();
  }

  private static @NonNull SetCandidate mapCandidate(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new SetCandidate(
        resultSet.getObject("measurement_set_id", UUID.class),
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        resultSet.getString("name"),
        resultSet.getString("note"),
        resultSet.getLong("version"),
        resultSet.getTimestamp("measured_at").toInstant()
    );
  }

  private static @NonNull RecordRow mapRecord(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new RecordRow(
        resultSet.getObject("measurement_set_id", UUID.class),
        FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class)),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        resultSet.getObject("record_id", UUID.class),
        resultSet.getString("name"),
        resultSet.getString("note"),
        resultSet.getLong("version"),
        characteristic,
        MeasurementJdbcReadSupport.value(resultSet, characteristic),
        resultSet.getTimestamp("measured_at").toInstant(),
        resultSet.getString("source_name"),
        nullableConfidence(resultSet.getString("confidence"))
    );
  }

  private static @Nullable ConfidenceLevel nullableConfidence(@Nullable String value) {
    return value == null ? null : ConfidenceLevel.valueOf(value);
  }

  private static @NonNull WorkspaceId workspaceId(
      @NonNull List<@NonNull SetCandidate> candidates,
      @NonNull FeedstockMeasurementId measurementId
  ) {
    return candidates.stream()
        .filter(candidate -> candidate.measurementSetId().equals(measurementId.value()))
        .map(SetCandidate::workspaceId)
        .findFirst()
        .orElseThrow();
  }

  private enum CursorDirection {
    ASCENDING(">", "ASC"),
    DESCENDING("<", "DESC");

    private final @NonNull String operator;
    private final @NonNull String order;

    CursorDirection(
        @NonNull String operator,
        @NonNull String order
    ) {
      this.operator = operator;
      this.order = order;
    }

    private @NonNull String operator() {
      return operator;
    }

    private @NonNull String order() {
      return order;
    }
  }

  private record SetCandidate(
      @NonNull UUID measurementSetId,
      @NonNull WorkspaceId workspaceId,
      @NonNull FeedstockBatchId batchId,
      @NonNull MaterialStateId materialStateId,
      @NonNull String name,
      @Nullable String note,
      long version,
      @NonNull Instant measuredAt
  ) {
  }

  private record RecordRow(
      @NonNull UUID measurementSetId,
      @NonNull FeedstockBatchId batchId,
      @NonNull MaterialStateId materialStateId,
      @NonNull UUID recordId,
      @NonNull String name,
      @Nullable String note,
      long version,
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Instant measuredAt,
      @Nullable String source,
      @Nullable ConfidenceLevel confidence
  ) {
  }

  private record SetMetadata(
      @NonNull UUID measurementSetId,
      @NonNull String name,
      @Nullable String note,
      long version
  ) {
  }

  private static @NonNull MeasurementSubjectReference subject(@NonNull FeedstockBatchId batchId) {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, batchId.value());
  }
}
