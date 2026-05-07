package org.craft.mcs.cap.shared.domain.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.domain.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import systems.uom.quantity.Acidity;
import systems.uom.ucum.UCUM;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


@ValueObject
public final class PhValue
    extends MeasuredValue<Acidity, PhValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("15.0");
  private static final Unit<Acidity> UNIT = UCUM.PH;

  public PhValue(@NonNull Quantity<Acidity> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static PhValue of(@NonNull BigDecimal ph) {
    requireNonNull(ph, "pH value cannot be null");
    return new PhValue(Quantities.getQuantity(ph, UNIT));
  }

  @NonNull
  public static PhValue of(@NonNull String ph) {
    requireNonNull(ph, "pH value cannot be null");
    return of(new BigDecimal(ph));
  }

  @NonNull
  public static PhValue of(double ph) {
    return of(BigDecimal.valueOf(ph));
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }
}
