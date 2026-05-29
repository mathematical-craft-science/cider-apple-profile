package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.UnsupportedChemicalMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.Tanninity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes external tannin quantities into mg/L TAE,
 * the canonical tannin unit used inside the service core.
 */
public final class TanninityQuantityNormalizer
    implements ExternalChemicalMeasurementNormalizer<TanninityValue> {

  private static final @NonNull String AXIS = "tanninity";
  private static final @NonNull String CANONICAL_UNIT = CanonicalUnitLabels.TANNINITY;

  @Override
  public @NonNull TanninityValue normalizeValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    return new TanninityValue(normalizeQuantity(value, unit));
  }

  @Override
  public @Nullable BigDecimal normalizeStandardDeviation(
      @NonNull BigDecimal mean,
      @Nullable BigDecimal standardDeviation,
      @Nullable String unit
  ) {
    requireNonNull(mean, "mean tanninity value cannot be null");
    if (standardDeviation == null) {
      return null;
    }
    return QuantityValues.bigDecimal(normalizeQuantity(standardDeviation, unit));
  }

  private static @NonNull Quantity<Tanninity> normalizeQuantity(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "tanninity value cannot be null");
    return Quantities
        .getQuantity(value, unit(unit))
        .to(BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID);
  }

  private static @NonNull Unit<Tanninity> unit(@Nullable String unit) {
    return switch (ExternalChemicalMeasurementUnits.normalize(unit, CANONICAL_UNIT)) {
      case "mg/l tae", "mg/l", "mg/l as tannic acid", "mg/l tannic acid" ->
          BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID;
      case "g/l tae", "g/l", "g/l as tannic acid", "g/l tannic acid" ->
          BeverageUnits.GRAM_PER_LITER_AS_TANNIC_ACID;
      default -> throw new UnsupportedChemicalMeasurementUnitException(AXIS, unit);
    };
  }
}
