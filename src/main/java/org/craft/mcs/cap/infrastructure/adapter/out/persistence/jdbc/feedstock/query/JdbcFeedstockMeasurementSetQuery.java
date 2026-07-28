package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockMeasurementAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockMeasurementAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementView;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.measurement.application.compatibility.MeasurementChemicalProfileProjector;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement.MeasurementJdbcReadSupport;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceSnapshot;
import org.craft.mcs.cap.shared.application.measurement.MeasurementProvenanceView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads individual active MeasurementSet revisions and maps them to Feedstock compatibility views. */
@Component
@RequiredArgsConstructor
class JdbcFeedstockMeasurementSetQuery implements FeedstockMeasurementAnalysisCandidateRepository {

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
        FROM measurement_sets s
        JOIN measurement_set_heads head
          ON head.measurement_set_id = s.measurement_set_id
        JOIN measurement_set_revisions revision
          ON revision.measurement_set_id = head.measurement_set_id
         AND revision.revision_id = head.revision_id
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

  @NonNull Optional<@NonNull FeedstockMeasurementView> findById(
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
        JdbcFeedstockMeasurementSetQuery::mapCandidate
    );
    return loadViews(candidates).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementView> findCandidates(
      @NonNull FeedstockMeasurementAnalysisCandidateQuery query
  ) {
    requireNonNull(query, "feedstock measurement analysis candidate query cannot be null");
    return loadViews(queryCandidates(
        query.workspaceId(),
        query.batch(),
        query.season(),
        query.after(),
        CursorDirection.ASCENDING,
        query.limit()
    ));
  }

  @NonNull List<@NonNull FeedstockMeasurementSummary> findSummariesByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    final var candidates = queryCandidates(
        null,
        FeedstockBatchCriteria.of(batchId),
        null,
        null,
        CursorDirection.DESCENDING,
        Integer.MAX_VALUE
    );
    return loadViews(candidates).stream()
        .map(view -> new FeedstockMeasurementSummary(
            view.measurementId(),
            workspaceId(candidates, view.measurementId()),
            view.batchId(),
            view.name(),
            view.measuredAt(),
            view.evidenceVersion(),
            view.chemicalProfile(),
            view.note()
        ))
        .toList();
  }

  private @NonNull List<@NonNull SetCandidate> queryCandidates(
      @Nullable WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season,
      @Nullable FeedstockAnalysisCandidatePagePosition cursor,
      @NonNull CursorDirection direction,
      int limit
  ) {
    final var sql = new StringBuilder(SET_CANDIDATES);
    final var parameters = new MapSqlParameterSource();
    FeedstockMeasurementSqlFilters.append(sql, parameters, workspaceId, batchCriteria, season);
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
    // SQL structure consists only of internal constant fragments; external values remain named parameters.
    //noinspection SqlSourceToSinkFlow
    return jdbcTemplate.query(sql.toString(), parameters, JdbcFeedstockMeasurementSetQuery::mapCandidate);
  }

  private @NonNull List<@NonNull FeedstockMeasurementView> loadViews(
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

  private @NonNull Map<UUID, List<RecordRow>> loadRecordsBySetIds(
      @NonNull List<@NonNull UUID> measurementSetIds
  ) {
    final var rows = jdbcTemplate.query(
        """
            SELECT s.measurement_set_id,
                   revision.material_state_id,
                   record.*
              FROM measurement_sets s
              JOIN measurement_set_heads head
                ON head.measurement_set_id = s.measurement_set_id
              JOIN measurement_set_revisions revision
                ON revision.measurement_set_id = head.measurement_set_id
               AND revision.revision_id = head.revision_id
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
        JdbcFeedstockMeasurementSetQuery::mapRecord
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

  private static @NonNull Optional<@NonNull ChemicalProfile> toChemicalProfile(
      @NonNull List<RecordRow> records
  ) {
    final var byCharacteristic =
        new EnumMap<MeasurementCharacteristic, CharacteristicValue<MeasuredValue>>(
            MeasurementCharacteristic.class
        );
    records.forEach(recordRow -> byCharacteristic.put(recordRow.characteristic(), recordRow.value()));
    return MeasurementChemicalProfileProjector.project(byCharacteristic);
  }

  private static @NonNull List<@NonNull MeasurementEvidenceSnapshot> toEvidence(
      @NonNull List<RecordRow> records
  ) {
    return records.stream()
        .map(recordRow -> new MeasurementEvidenceSnapshot(
            recordRow.characteristic(),
            MeasurementValueOrigin.MEASURED,
            recordRow.materialStateId(),
            new MeasurementEvidenceReference(recordRow.measurementSetId(), recordRow.recordId()),
            recordRow.measuredAt(),
            recordRow.provenance(),
            MeasurementEvidenceValidity.VALID
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
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        resultSet.getObject("record_id", UUID.class),
        characteristic,
        MeasurementJdbcReadSupport.value(resultSet, characteristic),
        resultSet.getTimestamp("measured_at").toInstant(),
        new MeasurementProvenanceView(
            MeasurementSourceType.valueOf(resultSet.getString("source_type")),
            resultSet.getString("source_name"),
            resultSet.getString("measurement_method"),
            resultSet.getString("source_reference"),
            nullableConfidence(resultSet.getString("confidence"))
        )
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
      @NonNull MaterialStateId materialStateId,
      @NonNull UUID recordId,
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Instant measuredAt,
      @NonNull MeasurementProvenanceView provenance
  ) {
  }
}
