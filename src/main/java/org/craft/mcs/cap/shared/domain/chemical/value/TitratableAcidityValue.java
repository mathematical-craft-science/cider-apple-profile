package org.craft.mcs.cap.shared.domain.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.domain.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.domain.measurement.unit.TitratableAcidity;
import org.craft.mcs.cap.shared.domain.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


@ValueObject
public final class TitratableAcidityValue
    extends MeasuredValue<TitratableAcidity, TitratableAcidityValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("30.0");
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

  @NonNull
  public static TitratableAcidityValue of(@NonNull String gramsPerLiter) {
    requireNonNull(gramsPerLiter, "titratable acidity value cannot be null");
    return of(new BigDecimal(gramsPerLiter));
  }

  @NonNull
  public static TitratableAcidityValue of(double gramsPerLiter) {
    return of(BigDecimal.valueOf(gramsPerLiter));
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }
}
