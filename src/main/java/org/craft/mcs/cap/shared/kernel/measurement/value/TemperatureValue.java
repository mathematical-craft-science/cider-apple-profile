package org.craft.mcs.cap.shared.kernel.measurement.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Temperature;
import org.craft.mcs.cap.shared.kernel.measurement.AbstractMeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/** Temperature expressed in the canonical CAP unit, degrees Celsius. */
@ValueObject
public final class TemperatureValue
    extends AbstractMeasuredValue<Temperature, TemperatureValue> {

  private static final BigDecimal ABSOLUTE_ZERO_CELSIUS = new BigDecimal("-273.15");
  private static final Unit<Temperature> UNIT = BeverageUnits.CELSIUS;

  public TemperatureValue(@NonNull Quantity<Temperature> value) {
    super(value, UNIT, ABSOLUTE_ZERO_CELSIUS, null);
  }

  public static @NonNull TemperatureValue of(@NonNull BigDecimal value) {
    requireNonNull(value, "temperature value cannot be null");
    return new TemperatureValue(Quantities.getQuantity(value, UNIT));
  }

  @Override
  public @NonNull TemperatureValue withValue(@NonNull BigDecimal value) {
    return of(value);
  }
}
