package org.craft.mcs.cap.shared.kernel.sugar;

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
 * Value object for sugar concentration represented as specific gravity.
 *
 * <p>Specific gravity is the canonical sugar unit inside CAP core. The value object enforces the
 * accepted domain range and exposes that range for transport metadata without leaking REST or UI
 * concerns into the domain model.
 */
@ValueObject
public final class SpecificGravityValue
    extends AbstractMeasuredValue<SpecificGravity, SpecificGravityValue>
    implements SugarValue {

  private static final BigDecimal MIN = new BigDecimal("1.000");
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
