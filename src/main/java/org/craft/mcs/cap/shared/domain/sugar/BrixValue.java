package org.craft.mcs.cap.shared.domain.sugar;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.domain.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.domain.measurement.unit.Brix;
import org.craft.mcs.cap.shared.domain.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

/**
 * Объект-значение представляющий значение шкалы Brix.
 */
@ValueObject
public final class BrixValue
    extends MeasuredValue<Brix, BrixValue>
    implements SugarValue {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("32.0");
  private static final Unit<Brix> UNIT = BeverageUnits.BRIX;

  public BrixValue(@NonNull Quantity<Brix> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static BrixValue of(@NonNull BigDecimal value) {
    return new BrixValue(Quantities.getQuantity(value, UNIT));
  }

  @NonNull
  public static BrixValue of(@NonNull String value) {
    return of(new BigDecimal(value));
  }
}