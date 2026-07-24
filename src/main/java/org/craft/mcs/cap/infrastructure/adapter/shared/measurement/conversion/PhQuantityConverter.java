package org.craft.mcs.cap.infrastructure.adapter.shared.measurement.conversion;

import java.math.BigDecimal;
import javax.measure.Quantity;
import org.craft.mcs.cap.shared.application.UnsupportedMeasurementUnitException;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import systems.uom.quantity.Acidity;
import tech.units.indriya.quantity.Quantities;

/**
 * Converts external pH quantities into the canonical pH scale.
 * pH is accepted as a canonical dimensionless acidity unit at the REST boundary.
 */
public final class PhQuantityConverter
    implements ExternalMeasurementConverter<PhValue> {

  private static final @NonNull String AXIS = "pH";
  private static final @NonNull String CANONICAL_UNIT = BeverageUnits.label(BeverageUnits.PH);

  @Override
  public @NonNull PhValue convertValue(
      @NonNull BigDecimal value,
      @Nullable String unit
  ) {
    if (!ExternalMeasurementUnits.unitKey(unit, CANONICAL_UNIT).equals("ph")) {
      throw new UnsupportedMeasurementUnitException(AXIS, unit);
    }
    return new PhValue(phQuantity(value));
  }

  private static @NonNull Quantity<Acidity> phQuantity(@NonNull BigDecimal value) {
    return Quantities.getQuantity(value, BeverageUnits.PH);
  }
}
