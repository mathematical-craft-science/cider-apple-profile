package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability;

import java.sql.Types;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row.BatchTraceStepJdbcRow;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row.BatchTraceStepJdbcRowMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row.BatchTraceStepSubjectJdbcRow;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;


/** Append-only JDBC adapter for immutable traceability facts. */
@Repository
@RequiredArgsConstructor
class JdbcBatchTraceabilityStore implements BatchTraceabilityStore {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;
  private final @NonNull Clock clock;

  @Override
  public @NonNull BatchTraceStep append(@NonNull BatchTraceStep step) {
    requireNonNull(step, "batch trace step cannot be null");

    try {
      final var row = BatchTraceStepJdbcRowMapper.fromDomain(step);
      insertStep(row);
      insertSubjects(row);
      return step;
    } catch (DuplicateKeyException exception) {
      throw new ConcurrentResourceModificationException("material state", exception);
    }
  }

  private void insertStep(@NonNull BatchTraceStepJdbcRow row) {
    jdbcTemplate.update("""
            INSERT INTO batch_trace_steps (
              step_id,
              workspace_id,
              parent_step_id,
              step_type,
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
              :stepType,
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
            .addValue("stepType", row.stepType.name())
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
              material_state_id,
              subject_type,
              subject_id,
              display_name_snapshot,
              invalidated_characteristics,
              snapshot_volume_liters,
              snapshot_confidence,
              sugar_value,
              sugar_value_kind,
              sugar_min,
              sugar_max,
              acidity_value,
              acidity_value_kind,
              acidity_min,
              acidity_max,
              tannin_value,
              tannin_value_kind,
              tannin_min,
              tannin_max,
              yan_value,
              yan_value_kind,
              yan_min,
              yan_max
            )
            VALUES (
              :stepId,
              :subjectRole,
              :subjectOrder,
              :materialStateId,
              :subjectType,
              :subjectId,
              :displayNameSnapshot,
              COALESCE(
                string_to_array(NULLIF(:invalidatedCharacteristics, ''), ','),
                ARRAY[]::VARCHAR[]
              ),
              :snapshotVolumeLiters,
              :snapshotConfidence,
              :sugarValue,
              :sugarValueKind,
              :sugarMin,
              :sugarMax,
              :acidityValue,
              :acidityValueKind,
              :acidityMin,
              :acidityMax,
              :tanninValue,
              :tanninValueKind,
              :tanninMin,
              :tanninMax,
              :yanValue,
              :yanValueKind,
              :yanMin,
              :yanMax
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
        .addValue("materialStateId", subject.materialStateId)
        .addValue("subjectType", subject.subjectType.name())
        .addValue("subjectId", subject.subjectId)
        .addValue("displayNameSnapshot", subject.displayNameSnapshot)
        .addValue(
            "invalidatedCharacteristics",
            subject.invalidatedCharacteristics.stream()
                .map(Enum::name)
                .sorted()
                .collect(joining(","))
        )
        .addValue("snapshotVolumeLiters", subject.snapshotVolumeLiters)
        .addValue("snapshotConfidence", subject.snapshotConfidence)
        .addValue("sugarValue", subject.sugarValue)
        .addValue("sugarValueKind", enumName(subject.sugarValueKind))
        .addValue("sugarMin", subject.sugarMin)
        .addValue("sugarMax", subject.sugarMax)
        .addValue("acidityValue", subject.acidityValue)
        .addValue("acidityValueKind", enumName(subject.acidityValueKind))
        .addValue("acidityMin", subject.acidityMin)
        .addValue("acidityMax", subject.acidityMax)
        .addValue("tanninValue", subject.tanninValue)
        .addValue("tanninValueKind", enumName(subject.tanninValueKind))
        .addValue("tanninMin", subject.tanninMin)
        .addValue("tanninMax", subject.tanninMax)
        .addValue("yanValue", subject.yeastAssimilableNitrogenValue)
        .addValue("yanValueKind", enumName(subject.yeastAssimilableNitrogenValueKind))
        .addValue("yanMin", subject.yeastAssimilableNitrogenMin)
        .addValue("yanMax", subject.yeastAssimilableNitrogenMax);
  }

  private static @Nullable String enumName(@Nullable Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static @NonNull OffsetDateTime timestamp(@NonNull Instant value) {
    return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }
}
