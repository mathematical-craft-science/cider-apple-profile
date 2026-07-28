package org.craft.mcs.cap.shared.kernel.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.SpecificGravity;
import org.craft.mcs.cap.shared.kernel.measurement.AbstractMeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;


/**
 * Canonical Specific Gravity measurement value.
 *
 * <p>Specific Gravity is physical measurement evidence. Analysis may interpret it as sugar
 * potential for unfermented material, but this value object does not encode that interpretation.
 * It therefore remains valid for dry cider values below {@code 1.000}.</p>
 */
@ValueObject
public final class SpecificGravityValue
    extends AbstractMeasuredValue<SpecificGravity, SpecificGravityValue> {

  private static final BigDecimal MIN = new BigDecimal("0.900");
  private static final BigDecimal MAX = new BigDecimal("1.200");
  private static final BigDecimal STEP = new BigDecimal("0.001");
  private static final Unit<SpecificGravity> UNIT = BeverageUnits.SG;

  public SpecificGravityValue(@NonNull Quantity<SpecificGravity> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static SpecificGravityValue of(@NonNull BigDecimal value) {
    return new SpecificGravityValue(Quantities.getQuantity(value, UNIT));
  }

  public static @NonNull BigDecimal minValue() {
    return MIN;
  }

  public static @NonNull BigDecimal maxValue() {
    return MAX;
  }

  public static @NonNull BigDecimal stepValue() {
    return STEP;
  }

  @NonNull
  public static SpecificGravityValue of(@NonNull String value) {
    return of(new BigDecimal(value));
  }

  @Override
  public @NonNull SpecificGravityValue withValue(@NonNull BigDecimal value) {
    return of(value);
  }
}
