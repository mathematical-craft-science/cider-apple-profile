package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.traceability.application.contract.MaterialStateSnapshot;
import org.craft.mcs.cap.core.traceability.application.port.out.MaterialStateHistoryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row.BatchTraceStepJdbcRowMapper;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row.BatchTraceStepSubjectJdbcRow;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row.BatchTraceStepSubjectRole;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.traceability.row.BatchTraceCharacteristicStorageKind;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC lookup for immutable state snapshots embedded in traceability steps. */
@Repository
@RequiredArgsConstructor
class JdbcMaterialStateHistoryStore implements MaterialStateHistoryStore {

  private final @NonNull NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public @NonNull Optional<@NonNull MaterialStateSnapshot> find(
      @NonNull MaterialStateId materialStateId
  ) {
    requireNonNull(materialStateId, "material state id cannot be null");
    return jdbcTemplate.query(
        """
            SELECT step.workspace_id,
                   trace_subject.*
             FROM batch_trace_step_subjects trace_subject
              JOIN batch_trace_steps step ON step.step_id = trace_subject.step_id
             WHERE trace_subject.material_state_id = :materialStateId
               AND trace_subject.subject_role = 'OUTPUT'
             ORDER BY step.occurred_at,
                      step.step_id
             LIMIT 1
            """,
        new MapSqlParameterSource("materialStateId", materialStateId.value()),
        JdbcMaterialStateHistoryStore::mapState
    ).stream().findFirst();
  }

  private static @NonNull MaterialStateSnapshot mapState(
      @NonNull ResultSet resultSet,
      int rowNumber
  ) throws SQLException {
    final var row = new BatchTraceStepSubjectJdbcRow(
        BatchTraceStepSubjectRole.valueOf(resultSet.getString("subject_role")),
        resultSet.getInt("subject_order"),
        resultSet.getObject("material_state_id", UUID.class),
        BatchTraceSubjectType.valueOf(resultSet.getString("subject_type")),
        resultSet.getObject("subject_id", UUID.class),
        resultSet.getString("display_name_snapshot")
    );
    final var invalidatedValues = (String[]) resultSet.getArray("invalidated_characteristics").getArray();
    Arrays.stream(invalidatedValues)
        .map(MeasurementCharacteristic::valueOf)
        .forEach(row.invalidatedCharacteristics::add);
    row.snapshotVolumeLiters = resultSet.getBigDecimal("snapshot_volume_liters");
    row.snapshotConfidence = resultSet.getString("snapshot_confidence");
    row.specificGravityValue = resultSet.getBigDecimal("sugar_value");
    row.specificGravityValueKind = enumValue(
        BatchTraceCharacteristicStorageKind.class,
        resultSet.getString("sugar_value_kind")
    );
    row.specificGravityMin = resultSet.getBigDecimal("sugar_min");
    row.specificGravityMax = resultSet.getBigDecimal("sugar_max");
    row.titratableAcidityValue = resultSet.getBigDecimal("acidity_value");
    row.titratableAcidityValueKind = enumValue(
        BatchTraceCharacteristicStorageKind.class,
        resultSet.getString("acidity_value_kind")
    );
    row.titratableAcidityMin = resultSet.getBigDecimal("acidity_min");
    row.titratableAcidityMax = resultSet.getBigDecimal("acidity_max");
    row.tanninValue = resultSet.getBigDecimal("tannin_value");
    row.tanninValueKind = enumValue(
        BatchTraceCharacteristicStorageKind.class,
        resultSet.getString("tannin_value_kind")
    );
    row.tanninMin = resultSet.getBigDecimal("tannin_min");
    row.tanninMax = resultSet.getBigDecimal("tannin_max");
    row.yeastAssimilableNitrogenValue = resultSet.getBigDecimal("yan_value");
    row.yeastAssimilableNitrogenValueKind = enumValue(
        BatchTraceCharacteristicStorageKind.class,
        resultSet.getString("yan_value_kind")
    );
    row.yeastAssimilableNitrogenMin = resultSet.getBigDecimal("yan_min");
    row.yeastAssimilableNitrogenMax = resultSet.getBigDecimal("yan_max");
    return new MaterialStateSnapshot(
        WorkspaceId.of(resultSet.getObject("workspace_id", UUID.class)),
        BatchTraceStepJdbcRowMapper.toDomainSubject(row)
    );
  }

  private static <T extends Enum<T>> @Nullable T enumValue(
      @NonNull Class<T> type,
      @Nullable String value
  ) {
    return value == null ? null : Enum.valueOf(type, value);
  }
}
