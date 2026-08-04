package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.measurement;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementProvenance;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementValueKind;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TemperatureValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TurbidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.method;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.reference;
import static org.craft.mcs.cap.infrastructure.adapter.shared.measurement.MeasurementProvenanceValueMapper.sourceName;


/** Shared JDBC mapping for immutable measurement values and provenance. */
public final class MeasurementJdbcReadSupport {

  private MeasurementJdbcReadSupport() {
  }

  public static @NonNull CharacteristicValue<MeasuredValue> value(
      @NonNull ResultSet resultSet,
      @NonNull MeasurementCharacteristic characteristic
  ) throws SQLException {
    final var kind = MeasurementValueKind.valueOf(resultSet.getString("value_kind"));
    return switch (kind) {
      case POINT -> PointCharacteristicValue.of(
          measuredValue(characteristic, resultSet.getBigDecimal("point_value"))
      );
      case INTERVAL -> IntervalCharacteristicValue.of(
          measuredValue(characteristic, resultSet.getBigDecimal("lower_value")),
          measuredValue(characteristic, resultSet.getBigDecimal("upper_value"))
      );
    };
  }

  private static @NonNull MeasuredValue measuredValue(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull BigDecimal value
  ) {
    requireNonNull(value, "persisted measurement value cannot be null");
    return switch (characteristic) {
      case SPECIFIC_GRAVITY -> SpecificGravityValue.of(value);
      case PH -> PhValue.of(value);
      case TITRATABLE_ACIDITY -> TitratableAcidityValue.of(value);
      case TANNIN -> TanninValue.of(value);
      case YAN -> YeastAssimilableNitrogenValue.of(value);
      case TEMPERATURE -> TemperatureValue.of(value);
      case TURBIDITY -> TurbidityValue.of(value);
    };
  }

  public static @NonNull MeasurementProvenance provenance(@NonNull ResultSet resultSet) throws SQLException {
    return new MeasurementProvenance(
        MeasurementSourceType.valueOf(resultSet.getString("source_type")),
        sourceName(resultSet.getString("source_name")),
        method(resultSet.getString("measurement_method")),
        reference(resultSet.getString("source_reference")),
        nullableEnum(resultSet.getString("confidence"), ConfidenceLevel.class)
    );
  }

  public static @NonNull Instant instant(
      @NonNull ResultSet resultSet,
      @NonNull String column
  ) throws SQLException {
    return requireNonNull(resultSet.getTimestamp(column), column + " cannot be null").toInstant();
  }

  public static <E extends Enum<E>> @Nullable E nullableEnum(
      @Nullable String value,
      @NonNull Class<E> type
  ) {
    return value == null ? null : Enum.valueOf(type, value);
  }
}
