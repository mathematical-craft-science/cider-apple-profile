package org.craft.mcs.cap.shared.kernel.measurement;

import java.math.BigDecimal;
import java.util.Objects;
import javax.measure.Quantity;
import javax.measure.Unit;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.math.DecimalComparisons;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tech.units.indriya.quantity.Quantities;

import static java.util.Objects.requireNonNull;


/**
 * Shared JSR-385 implementation for immutable measurement value objects.
 *
 * @param <Q> JSR-385 quantity type
 * @param <S> concrete self type used for type-safe comparison and recreation
 */
public abstract class AbstractMeasuredValue<
    Q extends Quantity<Q>,
    S extends AbstractMeasuredValue<Q, S>
> implements MeasuredValue {

  private final Quantity<Q> value;
  private final Unit<Q> unit;
  private final @Nullable BigDecimal min;
  private final @Nullable BigDecimal max;

  protected AbstractMeasuredValue(
      @NonNull Quantity<Q> value,
      @NonNull Unit<Q> unit,
      @Nullable BigDecimal min,
      @Nullable BigDecimal max
  ) {
    this.unit = requireNonNull(unit);
    this.min = min;
    this.max = max;

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

  private static @NonNull BigDecimal normalize(@NonNull Number number) {
    if (number instanceof BigDecimal bigDecimal) {
      return bigDecimal;
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

  public final @NonNull Quantity<Q> quantity() {
    return value;
  }

  @Override
  public final @NonNull BigDecimal asBigDecimal() {
    return normalize(value.getValue());
  }

  public final @NonNull Unit<Q> unit() {
    return unit;
  }

  @Override
  public abstract @NonNull S withValue(@NonNull BigDecimal value);

  private void validateRange(@NonNull BigDecimal candidate) {
    if (min != null
        && max != null
        && (DecimalComparisons.isLessThan(candidate, min)
            || DecimalComparisons.isGreaterThan(candidate, max))
    ) {
      throw new DomainRuleViolationException(
          "Value " + candidate + " out of range [" + min + ", " + max + "]"
      );
    }
    if (min != null && DecimalComparisons.isLessThan(candidate, min)) {
      throw new DomainRuleViolationException(
          "Value " + candidate + " is below minimum " + min
      );
    }
    if (max != null && DecimalComparisons.isGreaterThan(candidate, max)) {
      throw new DomainRuleViolationException(
          "Value " + candidate + " exceeds maximum " + max
      );
    }
  }

  @Override
  public final int compareTo(@NonNull MeasuredValue other) {
    requireNonNull(other, "measured value to compare cannot be null");
    if (getClass() != other.getClass()) {
      throw new DomainRuleViolationException("measurement values of different characteristics cannot be compared");
    }
    return asBigDecimal().compareTo(other.asBigDecimal());
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
    final var that = (MeasuredValue) other;
    return asBigDecimal().compareTo(that.asBigDecimal()) == 0;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getClass(), asBigDecimal().stripTrailingZeros());
  }
}
