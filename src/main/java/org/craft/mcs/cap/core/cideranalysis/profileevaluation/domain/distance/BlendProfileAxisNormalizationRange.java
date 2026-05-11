package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record BlendProfileAxisNormalizationRange(
    @NonNull BlendProfileAxis axis,
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public BlendProfileAxisNormalizationRange {
    requireNonNull(axis, "normalization axis cannot be null");
    requireNonNull(min, "normalization minimum cannot be null");
    requireNonNull(max, "normalization maximum cannot be null");
    if (min.compareTo(max) >= 0) {
      throw new IllegalArgumentException("normalization minimum must be less than maximum: " + axis);
    }
  }
}
