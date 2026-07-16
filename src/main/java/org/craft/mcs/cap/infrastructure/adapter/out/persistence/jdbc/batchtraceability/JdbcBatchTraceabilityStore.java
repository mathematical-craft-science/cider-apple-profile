package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.sql.Types;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepJdbcRow;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepJdbcRowMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepSubjectJdbcRow;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** Append-only JDBC adapter for immutable traceability facts. */
@Repository
@RequiredArgsConstructor
class JdbcBatchTraceabilityStore implements BatchTraceabilityStore {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;
  private final @NonNull Clock clock;

  @Override
  public @NonNull BatchTraceStep append(@NonNull BatchTraceStep step) {
    requireNonNull(step, "batch trace step cannot be null");

    final var row = BatchTraceStepJdbcRowMapper.fromDomain(step);
    insertStep(row);
    insertSubjects(row);

    return step;
  }

  private void insertStep(@NonNull BatchTraceStepJdbcRow row) {
    jdbcTemplate.update("""
            INSERT INTO batch_trace_steps (
              step_id,
              workspace_id,
              parent_step_id,
              operation_type,
              occurred_at,
              performed_by_type,
              performed_by_id,
              note,
              created_at
            )
            VALUES (
              :stepId,
              :workspaceId,
              :parentStepId,
              :operationType,
              :occurredAt,
              :performedByType,
              :performedById,
              :note,
              :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("stepId", row.stepId)
            .addValue("workspaceId", row.workspaceId)
            .addValue("parentStepId", row.parentStepId)
            .addValue("operationType", row.operationType.name())
            .addValue("occurredAt", timestamp(row.occurredAt), Types.TIMESTAMP_WITH_TIMEZONE)
            .addValue("performedByType", row.performedByType.name())
            .addValue("performedById", row.performedById)
            .addValue("note", row.note)
            .addValue("createdAt", timestamp(Instant.now(clock)), Types.TIMESTAMP_WITH_TIMEZONE)
    );
  }

  private void insertSubjects(@NonNull BatchTraceStepJdbcRow row) {
    final var batch = row.subjects.stream()
        .map(subject -> subjectParameters(row.stepId, subject))
        .toArray(MapSqlParameterSource[]::new);

    jdbcTemplate.batchUpdate("""
            INSERT INTO batch_trace_step_subjects (
              step_id,
              subject_role,
              subject_order,
              subject_type,
              subject_id,
              display_name_snapshot,
              snapshot_volume_liters,
              snapshot_confidence,
              sugar_value,
              sugar_value_kind,
              sugar_min,
              sugar_max,
              sugar_standard_deviation,
              sugar_sample_size,
              acidity_value,
              acidity_value_kind,
              acidity_min,
              acidity_max,
              acidity_standard_deviation,
              acidity_sample_size,
              tanninity_value,
              tanninity_value_kind,
              tanninity_min,
              tanninity_max,
              tanninity_standard_deviation,
              tanninity_sample_size,
              yan_value,
              yan_value_kind,
              yan_min,
              yan_max,
              yan_standard_deviation,
              yan_sample_size
            )
            VALUES (
              :stepId,
              :subjectRole,
              :subjectOrder,
              :subjectType,
              :subjectId,
              :displayNameSnapshot,
              :snapshotVolumeLiters,
              :snapshotConfidence,
              :sugarValue,
              :sugarValueKind,
              :sugarMin,
              :sugarMax,
              :sugarStandardDeviation,
              :sugarSampleSize,
              :acidityValue,
              :acidityValueKind,
              :acidityMin,
              :acidityMax,
              :acidityStandardDeviation,
              :aciditySampleSize,
              :tanninityValue,
              :tanninityValueKind,
              :tanninityMin,
              :tanninityMax,
              :tanninityStandardDeviation,
              :tanninitySampleSize,
              :yanValue,
              :yanValueKind,
              :yanMin,
              :yanMax,
              :yanStandardDeviation,
              :yanSampleSize
            )
            """,
        batch
    );
  }

  private static @NonNull MapSqlParameterSource subjectParameters(
      @NonNull UUID stepId,
      @NonNull BatchTraceStepSubjectJdbcRow subject
  ) {
    return new MapSqlParameterSource()
        .addValue("stepId", stepId)
        .addValue("subjectRole", subject.role.name())
        .addValue("subjectOrder", subject.subjectOrder)
        .addValue("subjectType", subject.subjectType.name())
        .addValue("subjectId", subject.subjectId)
        .addValue("displayNameSnapshot", subject.displayNameSnapshot)
        .addValue("snapshotVolumeLiters", subject.snapshotVolumeLiters)
        .addValue("snapshotConfidence", subject.snapshotConfidence)
        .addValue("sugarValue", subject.sugarValue)
        .addValue("sugarValueKind", enumName(subject.sugarValueKind))
        .addValue("sugarMin", subject.sugarMin)
        .addValue("sugarMax", subject.sugarMax)
        .addValue("sugarStandardDeviation", subject.sugarStandardDeviation)
        .addValue("sugarSampleSize", subject.sugarSampleSize)
        .addValue("acidityValue", subject.acidityValue)
        .addValue("acidityValueKind", enumName(subject.acidityValueKind))
        .addValue("acidityMin", subject.acidityMin)
        .addValue("acidityMax", subject.acidityMax)
        .addValue("acidityStandardDeviation", subject.acidityStandardDeviation)
        .addValue("aciditySampleSize", subject.aciditySampleSize)
        .addValue("tanninityValue", subject.tanninityValue)
        .addValue("tanninityValueKind", enumName(subject.tanninityValueKind))
        .addValue("tanninityMin", subject.tanninityMin)
        .addValue("tanninityMax", subject.tanninityMax)
        .addValue("tanninityStandardDeviation", subject.tanninityStandardDeviation)
        .addValue("tanninitySampleSize", subject.tanninitySampleSize)
        .addValue("yanValue", subject.yeastAssimilableNitrogenValue)
        .addValue("yanValueKind", enumName(subject.yeastAssimilableNitrogenValueKind))
        .addValue("yanMin", subject.yeastAssimilableNitrogenMin)
        .addValue("yanMax", subject.yeastAssimilableNitrogenMax)
        .addValue("yanStandardDeviation", subject.yeastAssimilableNitrogenStandardDeviation)
        .addValue("yanSampleSize", subject.yeastAssimilableNitrogenSampleSize);
  }

  private static @Nullable String enumName(@Nullable Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static @NonNull OffsetDateTime timestamp(@NonNull Instant value) {
    return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }
}
