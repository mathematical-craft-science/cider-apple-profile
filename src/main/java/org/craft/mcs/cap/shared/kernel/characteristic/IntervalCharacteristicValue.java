package org.craft.mcs.cap.shared.kernel.characteristic;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Represents characteristic bounds without implying observations or a statistical distribution.
 */
@ValueObject
public record IntervalCharacteristicValue<T extends Comparable<? super T>>(
    @NonNull T lower,
    @NonNull T upper
) implements CharacteristicValue<T> {

  public IntervalCharacteristicValue {
    requireNonNull(lower, "characteristic interval lower value cannot be null");
    requireNonNull(upper, "characteristic interval upper value cannot be null");

    if (lower.getClass() != upper.getClass()) {
      throw new DomainRuleViolationException("characteristic interval bounds must have the same value type");
    }
    if (lower.compareTo(upper) > 0) {
      throw new DomainRuleViolationException("characteristic interval lower value cannot exceed upper value");
    }
  }

  public static <T extends Comparable<? super T>> @NonNull IntervalCharacteristicValue<T> of(
      @NonNull T lower,
      @NonNull T upper
  ) {
    return new IntervalCharacteristicValue<>(lower, upper);
  }
}
