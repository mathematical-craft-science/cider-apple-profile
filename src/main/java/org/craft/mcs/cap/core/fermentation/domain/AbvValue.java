package org.craft.mcs.cap.core.fermentation.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValue;
import org.craft.mcs.cap.shared.kernel.measurement.unit.AlcoholByVolume;
import org.craft.mcs.cap.shared.kernel.measurement.unit.BeverageUnits;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


@ValueObject
public final class AbvValue
    extends MeasuredValue<AlcoholByVolume, AbvValue> {

  private static final int SCALE = 3;
  private static final BigDecimal MIN = BigDecimal.ZERO;
  private static final BigDecimal MAX = new BigDecimal("100.000");
  private static final Unit<AlcoholByVolume> UNIT = BeverageUnits.PERCENT_ABV;

  public AbvValue(@NonNull Quantity<AlcoholByVolume> value) {
    super(normalizeQuantity(value), UNIT, MIN, MAX);
  }

  @NonNull
  public static AbvValue of(@NonNull BigDecimal percent) {
    requireNonNull(percent, "percent must not be null");
    return new AbvValue(Quantities.getQuantity(round(percent), UNIT));
  }

  @NonNull
  public static AbvValue of(double percent) {
    return of(BigDecimal.valueOf(percent));
  }

  @NonNull
  public static AbvValue from(@NonNull Quantity<AlcoholByVolume> quantity) {
    return new AbvValue(quantity);
  }

  @NonNull
  private static Quantity<AlcoholByVolume> normalizeQuantity(
      @NonNull Quantity<AlcoholByVolume> quantity
  ) {
    requireNonNull(quantity, "ABV quantity cannot be null");
    final var normalized = quantity.to(UNIT);
    return Quantities.getQuantity(
        round(new BigDecimal(normalized.getValue().toString())),
        UNIT
    );
  }

  @NonNull
  private static BigDecimal round(@NonNull BigDecimal value) {
    return requireNonNull(value, "percent must not be null")
        .setScale(SCALE, RoundingMode.HALF_UP);
  }

  @NonNull
  public BigDecimal percent() {
    return asBigDecimal();
  }

  @NonNull
  public Quantity<AlcoholByVolume> toQuantity() {
    return quantity();
  }

  public boolean isZero() {
    return asBigDecimal().signum() == 0;
  }
}
