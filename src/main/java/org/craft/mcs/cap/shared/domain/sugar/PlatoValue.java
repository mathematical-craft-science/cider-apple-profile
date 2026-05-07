package org.craft.mcs.cap.shared.domain.sugar;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.domain.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.domain.measurement.unit.Plato;
import org.craft.mcs.cap.shared.domain.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;


@ValueObject
public final class PlatoValue
    extends MeasuredValue<Plato, PlatoValue>
    implements SugarValue {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("32.0");
  private static final Unit<Plato> UNIT = BeverageUnits.PLATO;

  public PlatoValue(@NonNull Quantity<Plato> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static PlatoValue of(@NonNull BigDecimal value) {
    return new PlatoValue(Quantities.getQuantity(value, UNIT));
  }

  @NonNull
  public static PlatoValue of(@NonNull String value) {
    return of(new BigDecimal(value));
  }
}