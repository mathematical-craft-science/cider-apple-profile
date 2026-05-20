package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.BatchTraceabilityStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceOperationType;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepJdbcRow;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepJdbcRowMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepSubjectJdbcRow;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepSubjectRole;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
class JdbcBatchTraceabilityRepository
    implements BatchTraceabilityStore, BatchTraceabilityQueryStore {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  private static @NonNull BatchTraceStepJdbcRow toStepRow(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var row = new BatchTraceStepJdbcRow(
        resultSet.getObject("step_id", UUID.class),
        resultSet.getObject("trace_id", UUID.class),
        resultSet.getObject("parent_step_id", UUID.class),
        BatchTraceOperationType.valueOf(resultSet.getString("operation_type")),
        instant(resultSet, "occurred_at"),
        resultSet.getString("performed_by"),
        resultSet.getString("note")
    );
    row.createdAt = instant(resultSet, "created_at");
    return row;
  }

  private static @NonNull StepSubjectRow toSubjectRow(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var subject = new BatchTraceStepSubjectJdbcRow(
        BatchTraceStepSubjectRole.valueOf(resultSet.getString("subject_role")),
        resultSet.getInt("subject_order"),
        BatchTraceSubjectType.valueOf(resultSet.getString("subject_type")),
        resultSet.getString("subject_id"),
        resultSet.getString("display_name_snapshot")
    );
    subject.snapshotVolumeLiters = resultSet.getBigDecimal("snapshot_volume_liters");
    subject.snapshotConfidence = resultSet.getString("snapshot_confidence");
    subject.sugarValue = resultSet.getBigDecimal("sugar_value");
    subject.sugarValueKind = enumValue(MeasurementValueKind.class, resultSet.getString("sugar_value_kind"));
    subject.sugarMin = resultSet.getBigDecimal("sugar_min");
    subject.sugarMax = resultSet.getBigDecimal("sugar_max");
    subject.sugarStandardDeviation = resultSet.getBigDecimal("sugar_standard_deviation");
    subject.sugarSampleSize = integerValue(resultSet, "sugar_sample_size");
    subject.acidityValue = resultSet.getBigDecimal("acidity_value");
    subject.acidityValueKind = enumValue(MeasurementValueKind.class, resultSet.getString("acidity_value_kind"));
    subject.acidityMin = resultSet.getBigDecimal("acidity_min");
    subject.acidityMax = resultSet.getBigDecimal("acidity_max");
    subject.acidityStandardDeviation = resultSet.getBigDecimal("acidity_standard_deviation");
    subject.aciditySampleSize = integerValue(resultSet, "acidity_sample_size");
    subject.tanninityValue = resultSet.getBigDecimal("tanninity_value");
    subject.tanninityValueKind = enumValue(MeasurementValueKind.class, resultSet.getString("tanninity_value_kind"));
    subject.tanninityMin = resultSet.getBigDecimal("tanninity_min");
    subject.tanninityMax = resultSet.getBigDecimal("tanninity_max");
    subject.tanninityStandardDeviation = resultSet.getBigDecimal("tanninity_standard_deviation");
    subject.tanninitySampleSize = integerValue(resultSet, "tanninity_sample_size");
    return new StepSubjectRow(resultSet.getObject("step_id", UUID.class), subject);
  }

  private static @Nullable String enumName(@Nullable Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static <T extends Enum<T>> @Nullable T enumValue(
      @NonNull Class<T> type,
      @Nullable String value
  ) {
    return value == null ? null : Enum.valueOf(type, value);
  }

  private static @Nullable Integer integerValue(
      @NonNull ResultSet resultSet,
      @NonNull String columnName
  ) throws SQLException {
    final var value = resultSet.getInt(columnName);
    return resultSet.wasNull() ? null : value;
  }

  private static @NonNull OffsetDateTime timestamp(@NonNull Instant value) {
    return OffsetDateTime.ofInstant(value, ZoneOffset.UTC);
  }

  private static @NonNull Instant instant(
      @NonNull ResultSet resultSet,
      @NonNull String columnName
  ) throws SQLException {
    return resultSet.getObject(columnName, OffsetDateTime.class).toInstant();
  }

  @Override
  public @NonNull BatchTraceStep append(@NonNull BatchTraceStep step) {
    requireNonNull(step, "batch trace step cannot be null");

    final var row = BatchTraceStepJdbcRowMapper.fromDomain(step);
    insertStep(row);
    insertSubjects(row);

    return step;
  }

  @Override
  public @NonNull List<@NonNull BatchTraceStep> findByInputSubject(
      @NonNull BatchTraceSubject subject
  ) {
    requireNonNull(subject, "batch trace subject cannot be null");

    final var stepIds = jdbcTemplate.queryForList("""
            SELECT step.step_id
            FROM batch_trace_steps step
            JOIN batch_trace_step_subjects subject ON subject.step_id = step.step_id
            WHERE subject.subject_role = :subjectRole
              AND subject.subject_type = :subjectType
              AND subject.subject_id = :subjectId
            GROUP BY step.step_id, step.occurred_at
            ORDER BY step.occurred_at ASC, step.step_id ASC
            """,
        new MapSqlParameterSource()
            .addValue("subjectRole", BatchTraceStepSubjectRole.INPUT.name())
            .addValue("subjectType", subject.type().name())
            .addValue("subjectId", subject.id()),
        UUID.class
    );
    if (stepIds.isEmpty()) {
      return List.of();
    }

    final var rows = findSteps(stepIds);
    addSubjects(rows);

    return rows.values().stream()
        .map(BatchTraceStepJdbcRowMapper::toDomain)
        .toList();
  }

  private void insertStep(@NonNull BatchTraceStepJdbcRow row) {
    jdbcTemplate.update("""
            INSERT INTO batch_trace_steps (
              step_id,
              trace_id,
              parent_step_id,
              operation_type,
              occurred_at,
              performed_by,
              note,
              created_at
            )
            VALUES (
              :stepId,
              :traceId,
              :parentStepId,
              :operationType,
              :occurredAt,
              :performedBy,
              :note,
              :createdAt
            )
            """,
        new MapSqlParameterSource()
            .addValue("stepId", row.stepId)
            .addValue("traceId", row.traceId)
            .addValue("parentStepId", row.parentStepId)
            .addValue("operationType", row.operationType.name())
            .addValue("occurredAt", timestamp(row.occurredAt), Types.TIMESTAMP_WITH_TIMEZONE)
            .addValue("performedBy", row.performedBy)
            .addValue("note", row.note)
            .addValue("createdAt", timestamp(Instant.now()), Types.TIMESTAMP_WITH_TIMEZONE)
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
              tanninity_sample_size
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
              :tanninitySampleSize
            )
            """,
        batch
    );
  }

  private @NonNull MapSqlParameterSource subjectParameters(
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
        .addValue("tanninitySampleSize", subject.tanninitySampleSize);
  }

  private @NonNull Map<@NonNull UUID, @NonNull BatchTraceStepJdbcRow> findSteps(
      @NonNull List<@NonNull UUID> stepIds
  ) {
    final var rows = jdbcTemplate.query("""
            SELECT
              step_id,
              trace_id,
              parent_step_id,
              operation_type,
              occurred_at,
              performed_by,
              note,
              created_at
            FROM batch_trace_steps
            WHERE step_id IN (:stepIds)
            ORDER BY occurred_at ASC, step_id ASC
            """,
        new MapSqlParameterSource("stepIds", stepIds),
        JdbcBatchTraceabilityRepository::toStepRow
    );

    final Map<@NonNull UUID, @NonNull BatchTraceStepJdbcRow> rowsById =
        new LinkedHashMap<>();
    rows.forEach(row -> rowsById.put(row.stepId, row));
    return rowsById;
  }

  private void addSubjects(@NonNull Map<@NonNull UUID, @NonNull BatchTraceStepJdbcRow> rows) {
    final var subjects = jdbcTemplate.query("""
            SELECT
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
              tanninity_sample_size
            FROM batch_trace_step_subjects
            WHERE step_id IN (:stepIds)
            ORDER BY step_id ASC, subject_role ASC, subject_order ASC
            """,
        new MapSqlParameterSource("stepIds", new ArrayList<>(rows.keySet())),
        JdbcBatchTraceabilityRepository::toSubjectRow
    );

    subjects.forEach(subjectRow -> {
      final var step = rows.get(subjectRow.stepId());
      if (step != null) {
        step.addSubject(subjectRow.subject());
      }
    });
  }

  private record StepSubjectRow(
      @NonNull UUID stepId,
      @NonNull BatchTraceStepSubjectJdbcRow subject
  ) {
  }
}
