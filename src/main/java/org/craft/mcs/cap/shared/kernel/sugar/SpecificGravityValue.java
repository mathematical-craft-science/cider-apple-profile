package org.craft.mcs.cap.shared.kernel.sugar;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.SpecificGravity;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;


@ValueObject
public final class SpecificGravityValue
    extends MeasuredValue<SpecificGravity, SpecificGravityValue>
    implements SugarValue {

  private static final BigDecimal MIN = new BigDecimal("1.000");
  private static final BigDecimal MAX = new BigDecimal("1.200");
  private static final Unit<SpecificGravity> UNIT = BeverageUnits.SG;

  public SpecificGravityValue(@NonNull Quantity<SpecificGravity> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static SpecificGravityValue of(@NonNull BigDecimal value) {
    return new SpecificGravityValue(Quantities.getQuantity(value, UNIT));
  }

  @NonNull
  public static SpecificGravityValue of(@NonNull String value) {
    return of(new BigDecimal(value));
  }
}