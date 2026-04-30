package org.craft.mcs.cap.shared;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.model.Brix;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

/**
 * Объект-значение представляющий значение шкалы Brix.
 */
public final class BrixValue
    extends MeasuredValue<Brix, BrixValue>
    implements SugarValue {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("32.0");
  private static final Unit<Brix> UNIT = BeverageUnits.BRIX;

  public BrixValue(@NonNull Quantity<Brix> value) {
    super(value, UNIT, MIN, MAX);
  }

  public static BrixValue of(@NonNull BigDecimal value) {
    return new BrixValue(Quantities.getQuantity(value, UNIT));
  }

  public static BrixValue of(String value) {
    return of(new BigDecimal(value));
  }
}