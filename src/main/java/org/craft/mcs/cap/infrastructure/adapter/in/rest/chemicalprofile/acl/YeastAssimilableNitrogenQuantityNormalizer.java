package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.UnsupportedChemicalMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.YeastAssimilableNitrogen;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes external YAN quantities into mg N/L,
 * the canonical yeast-assimilable nitrogen unit used inside the service core.
 */
public final class YeastAssimilableNitrogenQuantityNormalizer
    implements ExternalChemicalMeasurementNormalizer<YeastAssimilableNitrogenValue> {

  private static final @NonNull String AXIS = "yeastAssimilableNitrogen";
  private static final @NonNull String CANONICAL_UNIT = CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN;

  @Override
  public @NonNull YeastAssimilableNitrogenValue normalizeValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    return new YeastAssimilableNitrogenValue(normalizeQuantity(value, unit));
  }

  @Override
  public @Nullable BigDecimal normalizeStandardDeviation(
      @NonNull BigDecimal mean,
      @Nullable BigDecimal standardDeviation,
      @Nullable String unit
  ) {
    requireNonNull(mean, "mean YAN value cannot be null");
    if (standardDeviation == null) {
      return null;
    }
    return QuantityValues.bigDecimal(normalizeQuantity(standardDeviation, unit));
  }

  private static @NonNull Quantity<YeastAssimilableNitrogen> normalizeQuantity(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    requireNonNull(value, "YAN value cannot be null");
    return Quantities
        .getQuantity(value, unit(unit))
        .to(BeverageUnits.MILLIGRAM_NITROGEN_PER_LITER);
  }

  private static @NonNull Unit<YeastAssimilableNitrogen> unit(@Nullable String unit) {
    return switch (ExternalChemicalMeasurementUnits.normalize(unit, CANONICAL_UNIT)) {
      case "mg n/l", "mg/l n", "mg/l", "ppm", "ppm n" ->
          BeverageUnits.MILLIGRAM_NITROGEN_PER_LITER;
      default -> throw new UnsupportedChemicalMeasurementUnitException(AXIS, unit);
    };
  }
}
