package org.craft.mcs.cap.shared.kernel.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.Tannin;
import org.craft.mcs.cap.shared.kernel.measurement.AbstractMeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Value object for tannin content in CAP canonical tannin units.
 *
 * <p>Tannin is represented as mg/L TAE and can participate in target-aware composition analysis
 * when the scenario explicitly includes tannin. The value object centralizes range validation for
 * domain logic and transport metadata.
 */
@ValueObject
public final class TanninValue
    extends AbstractMeasuredValue<Tannin, TanninValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("12000");
  private static final BigDecimal STEP = BigDecimal.ONE;
  private static final Unit<Tannin> UNIT = BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID;

  public TanninValue(@NonNull Quantity<Tannin> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static TanninValue of(@NonNull BigDecimal mgPerLiter) {
    requireNonNull(mgPerLiter, "Tannin value cannot be null");
    return new TanninValue(Quantities.getQuantity(mgPerLiter, UNIT));
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
  public static TanninValue of(@NonNull String mgPerLiter) {
    requireNonNull(mgPerLiter, "Tannin value cannot be null");
    return of(new BigDecimal(mgPerLiter));
  }

  @NonNull
  public static TanninValue of(double mgPerLiter) {
    return of(BigDecimal.valueOf(mgPerLiter));
  }

  @Override
  public @NonNull TanninValue withValue(@NonNull BigDecimal value) {
    return of(value);
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }

  @Override
  @NonNull
  public String toString() {
    return asBigDecimal().stripTrailingZeros().toPlainString()
        + " "
        + BeverageUnits.label(UNIT);
  }
}
