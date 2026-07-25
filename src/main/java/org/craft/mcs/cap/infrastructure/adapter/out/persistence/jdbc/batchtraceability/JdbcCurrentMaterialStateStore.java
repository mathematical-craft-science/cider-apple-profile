package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.model.CurrentMaterialState;
import org.craft.mcs.cap.core.batchtraceability.application.model.MaterialStateSnapshot;
import org.craft.mcs.cap.core.batchtraceability.application.port.out.CurrentMaterialStateStore;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepJdbcRowMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepSubjectJdbcRow;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.batchtraceability.row.BatchTraceStepSubjectRole;
import org.craft.mcs.cap.shared.application.ConcurrentResourceModificationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC adapter for the rebuildable current material-state pointer and exact state snapshots. */
@Repository
@RequiredArgsConstructor
class JdbcCurrentMaterialStateStore implements CurrentMaterialStateStore {

  private static final String FIND_CURRENT = """
      SELECT current_state.workspace_id,
             current_state.subject_type,
             current_state.subject_id,
             current_state.current_state_id,
             current_state.version
        FROM batch_trace_current_states current_state
       WHERE current_state.subject_type = :subjectType
         AND current_state.subject_id = :subjectId
      """;
  private static final String FIND_CURRENT_FOR_CONSISTENCY = FIND_CURRENT + " FOR SHARE OF current_state";

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull Optional<@NonNull CurrentMaterialState> findCurrent(
      @NonNull BatchTraceSubject subject
  ) {
    requireNonNull(subject, "material state subject cannot be null");
    return queryCurrent(subject, FIND_CURRENT);
  }

  @Override
  public @NonNull Optional<@NonNull CurrentMaterialState> findCurrentForConsistency(
      @NonNull BatchTraceSubject subject
  ) {
    requireNonNull(subject, "material state subject cannot be null");
    return queryCurrent(subject, FIND_CURRENT_FOR_CONSISTENCY);
  }

  @Override
  public @NonNull Map<@NonNull BatchTraceSubject, @NonNull CurrentMaterialState> findCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull BatchTraceSubject> subjects
  ) {
    requireNonNull(workspaceId, "material state workspace id cannot be null");
    requireNonNull(subjects, "material state subjects cannot be null");
    if (subjects.isEmpty()) {
      return Map.of();
    }

    final Map<BatchTraceSubject, CurrentMaterialState> states = new LinkedHashMap<>();
    for (final var subjectType : BatchTraceSubjectType.values()) {
      final var subjectIds = subjects.stream()
          .filter(subject -> subject.type() == subjectType)
          .map(BatchTraceSubject::id)
          .toList();
      if (!subjectIds.isEmpty()) {
        jdbcTemplate.query(
            """
                SELECT workspace_id,
                       subject_type,
                       subject_id,
                       current_state_id,
                       version
                  FROM batch_trace_current_states
                 WHERE workspace_id = :workspaceId
                   AND subject_type = :subjectType
                   AND subject_id IN (:subjectIds)
                """,
            new MapSqlParameterSource()
                .addValue("workspaceId", workspaceId.value())
                .addValue("subjectType", subjectType.name())
                .addValue("subjectIds", subjectIds),
            JdbcCurrentMaterialStateStore::mapCurrentState
        ).forEach(state -> states.put(state.subject(), state));
      }
    }
    return Map.copyOf(states);
  }

  @Override
  public @NonNull Optional<@NonNull MaterialStateSnapshot> findState(
      @NonNull MaterialStateId materialStateId
  ) {
    requireNonNull(materialStateId, "material state id cannot be null");
    return jdbcTemplate.query(
        """
            SELECT step.workspace_id,
                   trace_subject.*
              FROM batch_trace_step_subjects trace_subject
              JOIN batch_trace_steps step ON step.step_id = trace_subject.step_id
             WHERE trace_subject.trace_state_id = :materialStateId
             ORDER BY CASE trace_subject.subject_role WHEN 'OUTPUT' THEN 0 ELSE 1 END,
                      step.occurred_at,
                      step.step_id
             LIMIT 1
            """,
        new MapSqlParameterSource("materialStateId", materialStateId.value()),
        JdbcCurrentMaterialStateStore::mapState
    ).stream().findFirst();
  }

  @Override
  public void initialize(
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchTraceSubject subject,
      @NonNull MaterialStateId initialStateId
  ) {
    requireNonNull(workspaceId, "material state workspace id cannot be null");
    requireNonNull(subject, "material state subject cannot be null");
    requireNonNull(initialStateId, "initial material state id cannot be null");
    try {
      jdbcTemplate.update(
          """
              INSERT INTO batch_trace_current_states (
                  workspace_id,
                  subject_type,
                  subject_id,
                  current_state_id,
                  version,
                  updated_at
              ) VALUES (
                  :workspaceId,
                  :subjectType,
                  :subjectId,
                  :currentStateId,
                  0,
                  CURRENT_TIMESTAMP
              )
              """,
          subjectParameters(subject)
              .addValue("workspaceId", workspaceId.value())
              .addValue("currentStateId", initialStateId.value())
      );
    } catch (DuplicateKeyException exception) {
      throw new ConcurrentResourceModificationException("current material state", exception);
    }
  }

  private static @NonNull MaterialStateSnapshot mapState(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var row = new BatchTraceStepSubjectJdbcRow(
        BatchTraceStepSubjectRole.valueOf(resultSet.getString("subject_role")),
        resultSet.getInt("subject_order"),
        resultSet.getObject("trace_state_id", UUID.class),
        BatchTraceSubjectType.valueOf(resultSet.getString("subject_type")),
        resultSet.getObject("subject_id", UUID.class),
        resultSet.getString("display_name_snapshot")
    );
    row.snapshotVolumeLiters = resultSet.getBigDecimal("snapshot_volume_liters");
    row.snapshotConfidence = resultSet.getString("snapshot_confidence");
    row.sugarValue = resultSet.getBigDecimal("sugar_value");
    row.sugarValueKind = enumValue(AxisValueKind.class, resultSet.getString("sugar_value_kind"));
    row.sugarMin = resultSet.getBigDecimal("sugar_min");
    row.sugarMax = resultSet.getBigDecimal("sugar_max");
    row.acidityValue = resultSet.getBigDecimal("acidity_value");
    row.acidityValueKind = enumValue(AxisValueKind.class, resultSet.getString("acidity_value_kind"));
    row.acidityMin = resultSet.getBigDecimal("acidity_min");
    row.acidityMax = resultSet.getBigDecimal("acidity_max");
    row.tanninValue = resultSet.getBigDecimal("tannin_value");
    row.tanninValueKind = enumValue(AxisValueKind.class, resultSet.getString("tannin_value_kind"));
    row.tanninMin = resultSet.getBigDecimal("tannin_min");
    row.tanninMax = resultSet.getBigDecimal("tannin_max");
    row.yeastAssimilableNitrogenValue = resultSet.getBigDecimal("yan_value");
    row.yeastAssimilableNitrogenValueKind = enumValue(
        AxisValueKind.class,
        resultSet.getString("yan_value_kind")
    );
    row.yeastAssimilableNitrogenMin = resultSet.getBigDecimal("yan_min");
    row.yeastAssimilableNitrogenMax = resultSet.getBigDecimal("yan_max");
    return new MaterialStateSnapshot(
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        BatchTraceStepJdbcRowMapper.toDomainSubject(row)
    );
  }

  private static @NonNull CurrentMaterialState mapCurrentState(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    return new CurrentMaterialState(
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        BatchTraceSubject.of(
            BatchTraceSubjectType.valueOf(resultSet.getString("subject_type")),
            resultSet.getObject("subject_id", UUID.class)
        ),
        MaterialStateId.of(resultSet.getObject("current_state_id", UUID.class)),
        resultSet.getLong("version")
    );
  }

  private @NonNull Optional<@NonNull CurrentMaterialState> queryCurrent(
      @NonNull BatchTraceSubject subject,
      @NonNull String query
  ) {
    return jdbcTemplate.query(
        query,
        subjectParameters(subject),
        JdbcCurrentMaterialStateStore::mapCurrentState
    ).stream().findFirst();
  }

  private static @NonNull MapSqlParameterSource subjectParameters(
      @NonNull BatchTraceSubject subject
  ) {
    return new MapSqlParameterSource()
        .addValue("subjectType", subject.type().name())
        .addValue("subjectId", subject.id());
  }

  private static <T extends Enum<T>> @Nullable T enumValue(
      @NonNull Class<T> type,
      @Nullable String value
  ) {
    return value == null ? null : Enum.valueOf(type, value);
  }
}
