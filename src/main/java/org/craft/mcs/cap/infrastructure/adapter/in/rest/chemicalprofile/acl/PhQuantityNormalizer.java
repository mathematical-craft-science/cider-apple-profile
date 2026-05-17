package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.acl;

import java.math.BigDecimal;
import javax.measure.Quantity;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.UnsupportedChemicalMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import systems.uom.quantity.Acidity;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Normalizes external pH quantities into the internal pH scale.
 * pH is accepted as a canonical dimensionless acidity unit at the REST boundary.
 */
public final class PhQuantityNormalizer
    implements ExternalChemicalMeasurementNormalizer<PhValue> {

  private static final @NonNull String AXIS = "pH";
  private static final @NonNull String CANONICAL_UNIT = "pH";

  @Override
  public @NonNull PhValue normalizeValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    if (!ExternalChemicalMeasurementUnits.normalize(unit, CANONICAL_UNIT).equals("ph")) {
      throw new UnsupportedChemicalMeasurementUnitException(AXIS, unit);
    }
    return new PhValue(phQuantity(value));
  }

  @Override
  public @Nullable BigDecimal normalizeStandardDeviation(
      @NonNull BigDecimal mean,
      @Nullable BigDecimal standardDeviation,
      @Nullable String unit
  ) {
    requireNonNull(mean, "mean pH value cannot be null");
    if (!ExternalChemicalMeasurementUnits.normalize(unit, CANONICAL_UNIT).equals("ph")) {
      throw new UnsupportedChemicalMeasurementUnitException(AXIS, unit);
    }
    return standardDeviation;
  }

  private static @NonNull Quantity<Acidity> phQuantity(@NonNull BigDecimal value) {
    return Quantities.getQuantity(value, BeverageUnits.PH);
  }
}
