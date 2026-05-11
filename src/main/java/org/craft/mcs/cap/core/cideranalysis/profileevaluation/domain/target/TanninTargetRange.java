package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record TanninTargetRange(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public TanninTargetRange {
    requireNonNull(min, "tannin target minimum cannot be null");
    requireNonNull(max, "tannin target maximum cannot be null");
    if (min.compareTo(max) > 0) {
      throw new IllegalArgumentException("tannin target minimum cannot be greater than maximum");
    }
  }

  public boolean contains(@NonNull BigDecimal value) {
    requireNonNull(value, "tannin value cannot be null");
    return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
  }

  public @NonNull BigDecimal nearestValueTo(@NonNull BigDecimal value) {
    requireNonNull(value, "tannin value cannot be null");
    if (value.compareTo(min) < 0) {
      return min;
    }
    if (value.compareTo(max) > 0) {
      return max;
    }
    return value;
  }
}
