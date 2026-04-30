package org.craft.mcs.cap.shared;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.model.Plato;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

public final class PlatoValue
    extends MeasuredValue<Plato, PlatoValue>
    implements SugarValue {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("32.0");
  private static final Unit<Plato> UNIT = BeverageUnits.PLATO;

  public PlatoValue(@NonNull Quantity<Plato> value) {
    super(value, UNIT, MIN, MAX);
  }

  public static PlatoValue of(@NonNull BigDecimal value) {
    return new PlatoValue(Quantities.getQuantity(value, UNIT));
  }

  public static PlatoValue of(String value) {
    return of(new BigDecimal(value));
  }
}