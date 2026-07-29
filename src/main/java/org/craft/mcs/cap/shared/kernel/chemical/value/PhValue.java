package org.craft.mcs.cap.shared.kernel.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.AbstractMeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import systems.uom.quantity.Acidity;
import systems.uom.ucum.UCUM;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Value object for canonical pH measurements.
 *
 * <p>pH is a dimensionless acidity measure used as supporting fermentation and safety context.
 * The class keeps pH validation centralized and exposes bounds for generic unit metadata.
 */
@ValueObject
public final class PhValue
    extends AbstractMeasuredValue<Acidity, PhValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("15.0");
  private static final BigDecimal STEP = new BigDecimal("0.01");
  private static final Unit<Acidity> UNIT = UCUM.PH;

  public PhValue(@NonNull Quantity<Acidity> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static PhValue of(@NonNull BigDecimal ph) {
    requireNonNull(ph, "pH value cannot be null");
    return new PhValue(Quantities.getQuantity(ph, UNIT));
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
  public static PhValue of(@NonNull String ph) {
    requireNonNull(ph, "pH value cannot be null");
    return of(new BigDecimal(ph));
  }

  @NonNull
  public static PhValue of(double ph) {
    return of(BigDecimal.valueOf(ph));
  }

  @Override
  public @NonNull PhValue withValue(@NonNull BigDecimal value) {
    return of(value);
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }
}
