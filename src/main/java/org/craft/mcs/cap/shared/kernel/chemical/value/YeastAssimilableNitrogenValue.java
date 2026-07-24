package org.craft.mcs.cap.shared.kernel.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.AbstractMeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.YeastAssimilableNitrogen;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Value object for yeast assimilable nitrogen in CAP canonical YAN units.
 *
 * <p>YAN is represented as mg N/L and is used for fermentation-start readiness and nutrient risk
 * assessment. It intentionally remains outside target distance axes so optimization can show
 * readiness trade-offs without treating YAN as a blend target coordinate.
 */
@ValueObject
public final class YeastAssimilableNitrogenValue
    extends AbstractMeasuredValue<YeastAssimilableNitrogen, YeastAssimilableNitrogenValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("2000.0");
  private static final BigDecimal STEP = BigDecimal.ONE;
  private static final Unit<YeastAssimilableNitrogen> UNIT =
      BeverageUnits.MILLIGRAM_NITROGEN_PER_LITER;

  public YeastAssimilableNitrogenValue(@NonNull Quantity<YeastAssimilableNitrogen> value) {
    super(value, UNIT, MIN, MAX);
  }

  public static @NonNull YeastAssimilableNitrogenValue of(@NonNull BigDecimal milligramsNitrogenPerLiter) {
    requireNonNull(milligramsNitrogenPerLiter, "YAN value cannot be null");
    return new YeastAssimilableNitrogenValue(Quantities.getQuantity(milligramsNitrogenPerLiter, UNIT));
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

  public static @NonNull YeastAssimilableNitrogenValue of(@NonNull String milligramsNitrogenPerLiter) {
    requireNonNull(milligramsNitrogenPerLiter, "YAN value cannot be null");
    return of(new BigDecimal(milligramsNitrogenPerLiter));
  }

  public static @NonNull YeastAssimilableNitrogenValue of(double milligramsNitrogenPerLiter) {
    return of(BigDecimal.valueOf(milligramsNitrogenPerLiter));
  }

  @Override
  public @NonNull YeastAssimilableNitrogenValue withValue(@NonNull BigDecimal value) {
    return of(value);
  }

  public @NonNull BigDecimal milligramsNitrogenPerLiter() {
    return asBigDecimal();
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }

  @Override
  public @NonNull String toString() {
    return asBigDecimal().stripTrailingZeros().toPlainString()
        + " "
        + BeverageUnits.label(UNIT);
  }
}
