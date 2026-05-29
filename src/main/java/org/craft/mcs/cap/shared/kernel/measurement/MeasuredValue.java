package org.craft.mcs.cap.shared.kernel.measurement;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import java.util.Objects;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.math.DecimalComparisons;
import org.jspecify.annotations.NonNull;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


public abstract class MeasuredValue<Q extends Quantity<Q>, S extends MeasuredValue<Q, S>>
    implements Comparable<S> {

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
      throw new DomainRuleViolationException(
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
    if (DecimalComparisons.isLessThan(v, min) || DecimalComparisons.isGreaterThan(v, max)) {
      throw new DomainRuleViolationException(
          "Value " + v + " out of range [" + min + ", " + max + "]"
      );
    }
  }

  @Override
  public final int compareTo(@NonNull S other) {
    requireNonNull(other);
    return this.asBigDecimal().compareTo(other.asBigDecimal());
  }

  public final boolean isLessThan(@NonNull S other) {
    requireNonNull(other, "measured value to compare cannot be null");
    return compareTo(other) < 0;
  }

  public final boolean isGreaterThan(@NonNull S other) {
    requireNonNull(other, "measured value to compare cannot be null");
    return compareTo(other) > 0;
  }

  public final boolean exceeds(@NonNull S limit) {
    requireNonNull(limit, "measured value limit cannot be null");
    return isGreaterThan(limit);
  }

  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    final var that = (MeasuredValue<?, ?>) other;
    return unit.equals(that.unit)
        && asBigDecimal().compareTo(that.asBigDecimal()) == 0;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getClass(), unit, asBigDecimal().stripTrailingZeros());
  }
}
