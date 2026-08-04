package org.craft.mcs.cap.shared.kernel.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.TitratableAcidity;
import org.craft.mcs.cap.shared.kernel.measurement.AbstractMeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Value object for titratable acidity in CAP canonical acidity units.
 *
 * <p>Inside core, titratable acidity is represented as g/L as malic acid. The class protects the
 * domain range and provides a single source of truth for transport metadata that needs accepted
 * input bounds.
 */
@ValueObject
public final class TitratableAcidityValue
    extends AbstractMeasuredValue<TitratableAcidity, TitratableAcidityValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("30.0");
  private static final BigDecimal STEP = new BigDecimal("0.1");
  private static final Unit<TitratableAcidity> UNIT =
      BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID;

  public TitratableAcidityValue(@NonNull Quantity<TitratableAcidity> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static TitratableAcidityValue of(@NonNull BigDecimal gramsPerLiter) {
    requireNonNull(gramsPerLiter, "titratable acidity value cannot be null");
    return new TitratableAcidityValue(Quantities.getQuantity(gramsPerLiter, UNIT));
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
  public static TitratableAcidityValue of(@NonNull String gramsPerLiter) {
    requireNonNull(gramsPerLiter, "titratable acidity value cannot be null");
    return of(new BigDecimal(gramsPerLiter));
  }

  @NonNull
  public static TitratableAcidityValue of(double gramsPerLiter) {
    return of(BigDecimal.valueOf(gramsPerLiter));
  }

  @Override
  public @NonNull TitratableAcidityValue withValue(@NonNull BigDecimal value) {
    return of(value);
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }
}
