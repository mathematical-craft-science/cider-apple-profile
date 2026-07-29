package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.measurement.application.model.CurrentMeasurementResultView;
import org.craft.mcs.cap.core.measurement.application.port.out.CurrentMeasurementResultQuery;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementRecordId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSetId;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectReference;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.core.traceability.application.model.MaterialStateLineage;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryMaterialStateLineageUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Comparator.reverseOrder;
import static java.util.Objects.requireNonNull;


/**
 * Set-based query for active measurement RESULT records applicable to Traceability-owned state lineages.
 *
 * <p>This adapter reads only Measurement tables. Material ancestry and invalidation rules are supplied
 * through the Traceability inbound boundary.</p>
 */
@Repository
@RequiredArgsConstructor
class JdbcCurrentMeasurementResultQuery implements CurrentMeasurementResultQuery {

  private static final String FIND_RESULTS = """
      SELECT measurement_set.subject_id,
             revision.material_state_id,
             measurement_set.measurement_set_id,
             record.record_id,
             record.characteristic,
             record.value_kind,
             record.measured_at,
             record.source_type,
             record.source_name,
             record.measurement_method,
             record.source_reference,
             record.confidence,
             record.point_value,
             record.lower_value,
             record.upper_value
        FROM measurement_sets measurement_set
        JOIN measurement_set_heads head
          ON head.measurement_set_id = measurement_set.measurement_set_id
        JOIN measurement_set_revisions revision
          ON revision.measurement_set_id = head.measurement_set_id
         AND revision.revision_id = head.revision_id
        JOIN measurement_revision_records revision_record
          ON revision_record.revision_id = revision.revision_id
        JOIN measurement_records record
          ON record.record_id = revision_record.record_id
         AND record.role = 'RESULT'
       WHERE measurement_set.workspace_id = :workspaceId
         AND measurement_set.subject_type = :subjectType
         AND measurement_set.subject_id IN (:subjectIds)
         AND measurement_set.lifecycle = 'ACTIVE'
         AND revision.material_state_id IN (:materialStateIds)
       ORDER BY measurement_set.subject_id,
                revision.material_state_id,
                record.characteristic,
                record.measured_at DESC,
                record.record_id DESC
      """;

  private static final Comparator<ApplicableResult> APPLICABILITY_ORDER =
      Comparator.comparingInt(ApplicableResult::depth)
          .thenComparing(ApplicableResult::measuredAt, reverseOrder())
          .thenComparing(ApplicableResult::recordIdOrder, reverseOrder());

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;
  private final @NonNull QueryMaterialStateLineageUseCase lineageQuery;

  @Override
  public @NonNull List<@NonNull CurrentMeasurementResultView> findByStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Map<@NonNull MeasurementSubjectReference, @NonNull MaterialStateId> currentStates
  ) {
    requireNonNull(workspaceId, "measurement workspace id cannot be null");
    requireNonNull(currentStates, "current measurement states cannot be null");
    if (currentStates.isEmpty()) {
      return List.of();
    }

    final var traceStates = currentStates.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> traceSubject(entry.getKey()),
            Map.Entry::getValue
        ));
    final var lineages = lineageQuery.findLineages(workspaceId, traceStates);
    final var results = new ArrayList<CurrentMeasurementResultView>();
    for (final var subjectType : MeasurementSubjectType.values()) {
      results.addAll(findBySubjectType(workspaceId, subjectType, lineages));
    }
    return List.copyOf(results);
  }

  private @NonNull List<@NonNull CurrentMeasurementResultView> findBySubjectType(
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectType subjectType,
      @NonNull Map<@NonNull BatchTraceSubject, @NonNull MaterialStateLineage> lineages
  ) {
    final var traceType = traceSubjectType(subjectType);
    final var matchingLineages = lineages.values().stream()
        .filter(lineage -> lineage.subject().type() == traceType)
        .toList();
    if (matchingLineages.isEmpty()) {
      return List.of();
    }

    final var evidenceResults = queryEvidenceResults(workspaceId, subjectType, matchingLineages);
    final Map<UUID, List<EvidenceResult>> evidenceBySubject = new LinkedHashMap<>();
    evidenceResults.forEach(result -> evidenceBySubject
        .computeIfAbsent(result.subjectId(), _ -> new ArrayList<>())
        .add(result));

    return matchingLineages.stream()
        .flatMap(lineage -> selectApplicableResults(
            subjectType,
            lineage,
            evidenceBySubject.getOrDefault(lineage.subject().id(), List.of())
        ).stream())
        .toList();
  }

  private @NonNull List<@NonNull EvidenceResult> queryEvidenceResults(
      @NonNull WorkspaceId workspaceId,
      @NonNull MeasurementSubjectType subjectType,
      @NonNull List<@NonNull MaterialStateLineage> lineages
  ) {
    final var subjectIds = lineages.stream()
        .map(MaterialStateLineage::subject)
        .map(BatchTraceSubject::id)
        .distinct()
        .toList();
    final var materialStateIds = lineages.stream()
        .flatMap(lineage -> lineage.states().stream())
        .map(state -> state.materialStateId().value())
        .distinct()
        .toList();
    return jdbcTemplate.query(
        FIND_RESULTS,
        new MapSqlParameterSource()
            .addValue("workspaceId", workspaceId.value())
            .addValue("subjectType", subjectType.name())
            .addValue("subjectIds", subjectIds)
            .addValue("materialStateIds", materialStateIds),
        JdbcCurrentMeasurementResultQuery::mapEvidenceResult
    );
  }

  private static @NonNull List<@NonNull CurrentMeasurementResultView> selectApplicableResults(
      @NonNull MeasurementSubjectType subjectType,
      @NonNull MaterialStateLineage lineage,
      @NonNull List<@NonNull EvidenceResult> evidenceResults
  ) {
    final var characteristics = evidenceResults.stream()
        .map(EvidenceResult::characteristic)
        .distinct()
        .toList();
    return characteristics.stream()
        .map(characteristic -> lineage.states().stream()
            .filter(state -> !state.invalidatedCharacteristics().contains(characteristic))
            .flatMap(state -> evidenceResults.stream()
                .filter(result -> result.materialStateId().equals(state.materialStateId()))
                .filter(result -> result.characteristic() == characteristic)
                .map(result -> new ApplicableResult(state.depth(), result)))
            .min(APPLICABILITY_ORDER)
            .map(result -> result.toCurrent(subjectType, lineage.currentMaterialStateId())))
        .flatMap(Optional::stream)
        .toList();
  }

  private static @NonNull EvidenceResult mapEvidenceResult(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var characteristic = MeasurementCharacteristic.valueOf(resultSet.getString("characteristic"));
    return new EvidenceResult(
        resultSet.getObject("subject_id", UUID.class),
        MaterialStateId.of(resultSet.getObject("material_state_id", UUID.class)),
        MeasurementSetId.of(resultSet.getObject("measurement_set_id", UUID.class)),
        MeasurementRecordId.of(resultSet.getObject("record_id", UUID.class)),
        characteristic,
        MeasurementJdbcReadSupport.value(resultSet, characteristic),
        MeasurementJdbcReadSupport.instant(resultSet, "measured_at"),
        MeasurementJdbcReadSupport.provenance(resultSet)
    );
  }

  private static @NonNull BatchTraceSubject traceSubject(
      @NonNull MeasurementSubjectReference subject
  ) {
    return BatchTraceSubject.of(traceSubjectType(subject.type()), subject.id());
  }

  private static @NonNull BatchTraceSubjectType traceSubjectType(
      @NonNull MeasurementSubjectType subjectType
  ) {
    return switch (subjectType) {
      case FEEDSTOCK_BATCH -> BatchTraceSubjectType.FEEDSTOCK_BATCH;
      case PRODUCTION_BATCH -> BatchTraceSubjectType.PRODUCTION_BATCH;
    };
  }

  private record EvidenceResult(
      @NonNull UUID subjectId,
      @NonNull MaterialStateId materialStateId,
      @NonNull MeasurementSetId measurementSetId,
      @NonNull MeasurementRecordId recordId,
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull CharacteristicValue<MeasuredValue> value,
      @NonNull Instant measuredAt,
      @NonNull MeasurementProvenance provenance
  ) {
  }

  private record ApplicableResult(
      int depth,
      @NonNull EvidenceResult result
  ) {

    private @NonNull Instant measuredAt() {
      return result.measuredAt();
    }

    private @NonNull String recordIdOrder() {
      return result.recordId().value().toString();
    }

    private @NonNull CurrentMeasurementResultView toCurrent(
        @NonNull MeasurementSubjectType subjectType,
        @NonNull MaterialStateId currentMaterialStateId
    ) {
      return new CurrentMeasurementResultView(
          new MeasurementSubjectReference(subjectType, result.subjectId()),
          currentMaterialStateId,
          result.materialStateId(),
          result.measurementSetId(),
          result.recordId(),
          result.characteristic(),
          result.value(),
          result.measuredAt(),
          result.provenance()
      );
    }
  }
}
