package org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.YeastAssimilableNitrogen;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Converts external YAN quantities into mg N/L,
 * the canonical yeast-assimilable nitrogen unit used inside the service core.
 */
public final class YeastAssimilableNitrogenQuantityConverter
    implements ExternalMeasurementConverter<YeastAssimilableNitrogenValue> {

  private static final @NonNull String AXIS = "yeastAssimilableNitrogen";
  private static final @NonNull String CANONICAL_UNIT = BeverageUnits.label(BeverageUnits.YAN);

  @Override
  public @NonNull YeastAssimilableNitrogenValue convertValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    return new YeastAssimilableNitrogenValue(convertQuantity(value, unit));
  }

  private static @NonNull Quantity<YeastAssimilableNitrogen> convertQuantity(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "YAN value cannot be null");
    return Quantities
        .getQuantity(value, unit(unit))
        .to(BeverageUnits.MILLIGRAM_NITROGEN_PER_LITER);
  }

  private static @NonNull Unit<YeastAssimilableNitrogen> unit(@Nullable String unit) {
    return switch (ExternalMeasurementUnits.unitKey(unit, CANONICAL_UNIT)) {
      case "mg n/l", "mg/l n", "mg/l", "ppm", "ppm n" ->
          BeverageUnits.MILLIGRAM_NITROGEN_PER_LITER;
      default -> throw new UnsupportedMeasurementUnitException(AXIS, unit);
    };
  }
}
