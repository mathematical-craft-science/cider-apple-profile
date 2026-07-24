package org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.Tannin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Converts external tannin quantities into mg/L TAE,
 * the canonical tannin unit used inside the service core.
 */
public final class TanninQuantityConverter
    implements ExternalMeasurementConverter<TanninValue> {

  private static final @NonNull String AXIS = "tannin";
  private static final @NonNull String CANONICAL_UNIT =
      BeverageUnits.label(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID);

  @Override
  public @NonNull TanninValue convertValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    return new TanninValue(convertQuantity(value, unit));
  }

  private static @NonNull Quantity<Tannin> convertQuantity(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "tannin value cannot be null");
    return Quantities
        .getQuantity(value, unit(unit))
        .to(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID);
  }

  private static @NonNull Unit<Tannin> unit(@Nullable String unit) {
    return switch (ExternalMeasurementUnits.unitKey(unit, CANONICAL_UNIT)) {
      case "mg/l tae", "mg/l", "mg/l as tannic acid", "mg/l tannic acid" ->
          BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID;
      case "g/l tae", "g/l", "g/l as tannic acid", "g/l tannic acid" ->
          BeverageUnits.GRAM_PER_LITER_AS_TANNIC_ACID;
      default -> throw new UnsupportedMeasurementUnitException(AXIS, unit);
    };
  }
}
