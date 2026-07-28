package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.query;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockLatestMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.port.out.FeedstockCurrentProfileQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementId;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.measurement.application.compatibility.CurrentMeasurementProfileView;
import org.craft.mcs.cap.core.measurement.application.compatibility.ProjectCurrentMeasurementProfileUseCase;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceReference;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads state-aware composite current profiles for Feedstock batches. */
@Component
@RequiredArgsConstructor
class JdbcFeedstockCurrentProfileQuery
    implements FeedstockCurrentProfileQueryRepository, FeedstockCurrentProfileAnalysisCandidateRepository {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;
  private final @NonNull ProjectCurrentMeasurementProfileUseCase currentProfileProjector;

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findCandidates(
      @NonNull CurrentFeedstockBatchAnalysisCandidateQuery query
  ) {
    requireNonNull(query, "current feedstock batch analysis candidate query cannot be null");
    return loadViews(queryBatchIds(
        query.workspaceId(),
        query.batch(),
        query.season(),
        query.after(),
        query.limit()
    ));
  }

  @Override
  public @NonNull Optional<@NonNull FeedstockCurrentProfileView> findByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return loadViews(List.of(batchId)).stream().findFirst();
  }

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findByBatchIds(
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    return loadViews(List.copyOf(batchIds));
  }

  private @NonNull List<@NonNull FeedstockBatchId> queryBatchIds(
      @NonNull WorkspaceId workspaceId,
      @Nullable FeedstockBatchCriteria batchCriteria,
      @Nullable FeedstockSeason season,
      @Nullable FeedstockAnalysisCandidatePagePosition cursor,
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
              JOIN measurement_set_heads head
                ON head.measurement_set_id = s.measurement_set_id
              JOIN measurement_set_revisions revision
                ON revision.measurement_set_id = head.measurement_set_id
               AND revision.revision_id = head.revision_id
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
    FeedstockMeasurementSqlFilters.append(sql, parameters, workspaceId, batchCriteria, season);
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
               measured_at < :afterMeasuredAt
               OR (
                   measured_at = :afterMeasuredAt
                   AND anchor_set_id < :afterMeasurementSetId
               )
           )
          """);
      parameters
          .addValue("afterMeasuredAt", cursor.measuredAt())
          .addValue("afterMeasurementSetId", cursor.measurementId());
    }
    sql.append("""
         ORDER BY measured_at DESC, anchor_set_id DESC
         LIMIT :limit
        """);
    parameters.addValue("limit", limit);
    // SQL structure consists only of internal constant fragments; external values remain named parameters.
    //noinspection SqlSourceToSinkFlow
    return jdbcTemplate.query(
        sql.toString(),
        parameters,
        (resultSet, rowNumber) -> FeedstockBatchId.of(resultSet.getObject("batch_id", UUID.class))
    );
  }

  private @NonNull List<@NonNull FeedstockCurrentProfileView> loadViews(
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
              .map(JdbcFeedstockCurrentProfileQuery::subject)
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
        .map(MeasurementEvidenceReference::measurementSetId)
        .collect(Collectors.toUnmodifiableSet()));

    return batchIds.stream()
        .map(batchId -> toView(batchId, profilesByBatch.get(batchId), metadataBySet))
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
                  JOIN measurement_set_heads head
                    ON head.measurement_set_id = measurement_set.measurement_set_id
                  JOIN measurement_set_revisions revision
                    ON revision.measurement_set_id = head.measurement_set_id
                   AND revision.revision_id = head.revision_id
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

  private static @NonNull Optional<@NonNull FeedstockCurrentProfileView> toView(
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
    return Optional.of(new FeedstockCurrentProfileView(
        batchId,
        profile.currentMaterialStateId(),
        profile.chemicalProfile(),
        profile.evidence(),
        new FeedstockLatestMeasurementView(
            FeedstockMeasurementId.of(anchorReference.measurementSetId()),
            FeedstockMeasurementName.of(metadata.name()),
            anchor.measuredAt(),
            metadata.version(),
            metadata.note()
        )
    ));
  }

  private static @NonNull MeasurementSubjectReference subject(@NonNull FeedstockBatchId batchId) {
    return new MeasurementSubjectReference(MeasurementSubjectType.FEEDSTOCK_BATCH, batchId.value());
  }

  private record SetMetadata(
      @NonNull UUID measurementSetId,
      @NonNull String name,
      @Nullable String note,
      long version
  ) {
  }
}
