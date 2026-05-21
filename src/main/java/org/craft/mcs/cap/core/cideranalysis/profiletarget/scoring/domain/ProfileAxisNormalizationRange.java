package org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isAtLeast;


public record ProfileAxisNormalizationRange(
    @NonNull ProfileEvaluationAxis axis,
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public ProfileAxisNormalizationRange {
    requireNonNull(axis, "normalization axis cannot be null");
    requireNonNull(min, "normalization minimum cannot be null");
    requireNonNull(max, "normalization maximum cannot be null");
    if (isAtLeast(min, max)) {
      throw new DomainRuleViolationException("normalization minimum must be less than maximum: " + axis);
    }
  }
}
