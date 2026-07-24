package org.craft.mcs.cap.infrastructure.adapter.in.measurement;

import java.math.BigDecimal;
import java.util.Locale;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.PhQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.SugarQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.TanninQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.TitratableAcidityQuantityConverter;
import org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion.YeastAssimilableNitrogenQuantityConverter;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.value.TemperatureValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.TurbidityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Converts inbound quantities into canonical measurement values before application commands are created. */
@Component
public final class MeasurementValueInputAcl {

  private final SugarQuantityConverter sugarConverter = new SugarQuantityConverter();
  private final PhQuantityConverter phConverter = new PhQuantityConverter();
  private final TitratableAcidityQuantityConverter acidityConverter =
      new TitratableAcidityQuantityConverter();
  private final TanninQuantityConverter tanninConverter = new TanninQuantityConverter();
  private final YeastAssimilableNitrogenQuantityConverter yanConverter =
      new YeastAssimilableNitrogenQuantityConverter();

  public @NonNull PointCharacteristicValue<MeasuredValue> point(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    return PointCharacteristicValue.of(measuredValue(characteristic, value, unit));
  }

  public @NonNull IntervalCharacteristicValue<MeasuredValue> interval(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull BigDecimal lower,
      @NonNull BigDecimal upper,
      @NonNull String unit
  ) {
    return IntervalCharacteristicValue.of(
        measuredValue(characteristic, lower, unit),
        measuredValue(characteristic, upper, unit)
    );
  }

  private @NonNull MeasuredValue measuredValue(
      @NonNull MeasurementCharacteristic characteristic,
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    requireNonNull(characteristic, "measurement characteristic cannot be null");
    requireNonNull(value, "measurement value cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");
    return switch (characteristic) {
      case SPECIFIC_GRAVITY -> sugarConverter.convertValue(value, unit).specificGravity();
      case PH -> phConverter.convertValue(value, unit);
      case TITRATABLE_ACIDITY -> acidityConverter.convertValue(value, unit);
      case TANNIN -> tanninConverter.convertValue(value, unit);
      case YAN -> yanConverter.convertValue(value, unit);
      case TEMPERATURE -> TemperatureValue.of(requireCanonicalEquivalentUnit(
          value,
          unit,
          BeverageUnits.label(BeverageUnits.CELSIUS),
          "cel",
          "c",
          "celsius"
      ));
      case TURBIDITY -> TurbidityValue.of(requireCanonicalEquivalentUnit(
          value,
          unit,
          BeverageUnits.label(BeverageUnits.NTU),
          BeverageUnits.label(BeverageUnits.FTU)
      ));
    };
  }

  private static @NonNull BigDecimal requireCanonicalEquivalentUnit(
      @NonNull BigDecimal value,
      @NonNull String unit,
      @NonNull String... acceptedUnits
  ) {
    final var unitKey = unit.strip().toLowerCase(Locale.ROOT);
    for (final var acceptedUnit : acceptedUnits) {
      if (unitKey.equals(acceptedUnit.strip().toLowerCase(Locale.ROOT))) {
        return value;
      }
    }
    throw new UnsupportedMeasurementUnitException("measurement", unit);
  }
}
