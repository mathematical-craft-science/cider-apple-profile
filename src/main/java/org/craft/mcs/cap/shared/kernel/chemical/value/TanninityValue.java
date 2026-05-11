package org.craft.mcs.cap.shared.kernel.chemical.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.craft.mcs.cap.shared.kernel.measurement.unit.Tanninity;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


@ValueObject
public final class TanninityValue
    extends MeasuredValue<Tanninity, TanninityValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("4000");
  private static final Unit<Tanninity> UNIT = BeverageUnits.MILLIGRAM_PER_LITER_AS_TANNIC_ACID;

  public TanninityValue(@NonNull Quantity<Tanninity> value) {
    super(value, UNIT, MIN, MAX);
  }

  @NonNull
  public static TanninityValue of(@NonNull BigDecimal mgPerLiter) {
    requireNonNull(mgPerLiter, "Tanninity value cannot be null");
    return new TanninityValue(Quantities.getQuantity(mgPerLiter, UNIT));
  }

  @NonNull
  public static TanninityValue of(@NonNull String mgPerLiter) {
    requireNonNull(mgPerLiter, "Tanninity value cannot be null");
    return of(new BigDecimal(mgPerLiter));
  }

  @NonNull
  public static TanninityValue of(double mgPerLiter) {
    return of(BigDecimal.valueOf(mgPerLiter));
  }

  public double asDouble() {
    return asBigDecimal().doubleValue();
  }

  @Override
  @NonNull
  public String toString() {
    return asBigDecimal().stripTrailingZeros().toPlainString() + " mg/L TAE";
  }
}
