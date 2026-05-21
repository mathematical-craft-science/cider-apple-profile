package org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isBetweenInclusive;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isGreaterThan;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isLessThan;


public record TanninTargetRange(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public TanninTargetRange {
    requireNonNull(min, "tannin target minimum cannot be null");
    requireNonNull(max, "tannin target maximum cannot be null");
    if (isGreaterThan(min, max)) {
      throw new DomainRuleViolationException("tannin target minimum cannot be greater than maximum");
    }
  }

  public boolean contains(@NonNull BigDecimal value) {
    requireNonNull(value, "tannin value cannot be null");
    return isBetweenInclusive(value, min, max);
  }

  public @NonNull BigDecimal nearestValueTo(@NonNull BigDecimal value) {
    requireNonNull(value, "tannin value cannot be null");
    if (isLessThan(value, min)) {
      return min;
    }
    if (isGreaterThan(value, max)) {
      return max;
    }
    return value;
  }
}
