package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.ChemicalProfileRestUnits;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.UnsupportedChemicalMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes external titratable-acidity quantities into g/L as malic acid,
 * the canonical acidity unit used inside the service core.
 */
public final class TitratableAcidityQuantityNormalizer
    implements ExternalChemicalMeasurementNormalizer<TitratableAcidityValue> {

  private static final @NonNull String AXIS = "titratable acidity";
  private static final @NonNull String CANONICAL_UNIT = ChemicalProfileRestUnits.TITRATABLE_ACIDITY;

  @Override
  public @NonNull TitratableAcidityValue normalizeValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    return new TitratableAcidityValue(normalizeQuantity(value, unit));
  }

  @Override
  public @Nullable BigDecimal normalizeStandardDeviation(
      @NonNull BigDecimal mean,
      @Nullable BigDecimal standardDeviation,
      @Nullable String unit
  ) {
    requireNonNull(mean, "mean titratable acidity value cannot be null");
    if (standardDeviation == null) {
      return null;
    }
    return QuantityValues.bigDecimal(normalizeQuantity(standardDeviation, unit));
  }

  private static @NonNull Quantity<TitratableAcidity> normalizeQuantity(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "titratable acidity value cannot be null");
    return Quantities
        .getQuantity(value, unit(unit))
        .to(BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID);
  }

  private static @NonNull Unit<TitratableAcidity> unit(@Nullable String unit) {
    return switch (ExternalChemicalMeasurementUnits.normalize(unit, CANONICAL_UNIT)) {
      case "g/l as malic acid", "g/l malic", "malic", "ta" -> BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID;
      case "g/l as lactic acid", "g/l lactic", "lactic" -> BeverageUnits.GRAM_PER_LITRE_AS_LACTIC_ACID;
      case "g/l as citric acid", "g/l citric", "citric" -> BeverageUnits.GRAM_PER_LITRE_AS_CITRIC_ACID;
      default -> throw new UnsupportedChemicalMeasurementUnitException(AXIS, unit);
    };
  }
}
