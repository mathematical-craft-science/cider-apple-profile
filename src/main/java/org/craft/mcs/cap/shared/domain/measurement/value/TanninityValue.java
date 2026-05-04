package org.craft.mcs.cap.shared.domain.measurement.value;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.domain.measurement.model.BeverageUnits;
import org.craft.mcs.cap.shared.domain.measurement.model.Tanninity;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


@ValueObject
public final class TanninityValue
    extends MeasuredValue<Tanninity, TanninityValue> {

  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("3000");
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
