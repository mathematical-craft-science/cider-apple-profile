package org.craft.mcs.cap.shared.kernel.measurement;

import java.math.BigDecimal;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


public abstract class MeasuredValue<Q extends Quantity<Q>, SELF extends MeasuredValue<Q, SELF>>
    implements Comparable<SELF> {

  private final Quantity<Q> value;
  private final Unit<Q> unit;
  private final BigDecimal min;
  private final BigDecimal max;

  protected MeasuredValue(
      @NonNull Quantity<Q> value,
      @NonNull Unit<Q> unit,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    this.unit = requireNonNull(unit);
    this.min = requireNonNull(min);
    this.max = requireNonNull(max);

    requireNonNull(value);

    if (!unit.equals(value.getUnit())) {
      throw new IllegalArgumentException(
          "Unit mismatch. Expected " + unit + ", got " + value.getUnit()
      );
    }

    final var normalized = normalize(value.getValue());
    validateRange(normalized);

    this.value = Quantities.getQuantity(normalized, unit);
  }

  @NonNull
  private static BigDecimal normalize(@NonNull Number number) {
    if (number instanceof BigDecimal bd) {
      return bd;
    }

    if (number instanceof Byte
        || number instanceof Short
        || number instanceof Integer
        || number instanceof Long
    ) {
      return BigDecimal.valueOf(number.longValue());
    }

    return new BigDecimal(number.toString());
  }

  @NonNull
  public final Quantity<Q> quantity() {
    return value;
  }

  @NonNull
  public final BigDecimal asBigDecimal() {
    return normalize(value.getValue());
  }

  @NonNull
  public final Unit<Q> unit() {
    return unit;
  }

  private void validateRange(@NonNull BigDecimal v) {
    if (v.compareTo(min) < 0 || v.compareTo(max) > 0) {
      throw new IllegalArgumentException(
          "Value " + v + " out of range [" + min + ", " + max + "]"
      );
    }
  }

  @Override
  public final int compareTo(@NonNull SELF other) {
    requireNonNull(other);
    return this.asBigDecimal().compareTo(other.asBigDecimal());
  }
}
