package org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Converts external titratable-acidity quantities into g/L as malic acid,
 * the canonical acidity unit used inside the service core.
 */
public final class TitratableAcidityQuantityConverter
    implements ExternalMeasurementConverter<TitratableAcidityValue> {

  private static final @NonNull String AXIS = "titratable acidity";
  private static final @NonNull String CANONICAL_UNIT =
      BeverageUnits.label(BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID);

  @Override
  public @NonNull TitratableAcidityValue convertValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    return new TitratableAcidityValue(convertQuantity(value, unit));
  }

  private static @NonNull Quantity<TitratableAcidity> convertQuantity(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "titratable acidity value cannot be null");
    return Quantities
        .getQuantity(value, unit(unit))
        .to(BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID);
  }

  private static @NonNull Unit<TitratableAcidity> unit(@Nullable String unit) {
    return switch (ExternalMeasurementUnits.unitKey(unit, CANONICAL_UNIT)) {
      case "g/l as malic acid", "g/l malic", "malic", "ta" -> BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID;
      case "g/l as lactic acid", "g/l lactic", "lactic" -> BeverageUnits.GRAM_PER_LITRE_AS_LACTIC_ACID;
      case "g/l as citric acid", "g/l citric", "citric" -> BeverageUnits.GRAM_PER_LITRE_AS_CITRIC_ACID;
      default -> throw new UnsupportedMeasurementUnitException(AXIS, unit);
    };
  }
}
