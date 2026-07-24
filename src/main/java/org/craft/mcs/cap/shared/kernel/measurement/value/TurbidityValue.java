package org.craft.mcs.cap.shared.kernel.measurement.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.AbstractMeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.Turbidity;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/** Turbidity expressed in the canonical CAP unit, NTU. */
@ValueObject
public final class TurbidityValue
    extends AbstractMeasuredValue<Turbidity, TurbidityValue> {

  private static final Unit<Turbidity> UNIT = BeverageUnits.NTU;

  public TurbidityValue(@NonNull Quantity<Turbidity> value) {
    super(value, UNIT, BigDecimal.ZERO, null);
  }

  public static @NonNull TurbidityValue of(@NonNull BigDecimal value) {
    requireNonNull(value, "turbidity value cannot be null");
    return new TurbidityValue(Quantities.getQuantity(value, UNIT));
  }

  @Override
  public @NonNull TurbidityValue withValue(@NonNull BigDecimal value) {
    return of(value);
  }
}
