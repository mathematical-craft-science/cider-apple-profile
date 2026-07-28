package org.craft.mcs.cap.core.profileanalysis.classification.domain.engine;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Numeric bounds retained solely for classification boundary-overlap evidence. */
public record ClassificationInterval(
    @NonNull BigDecimal lower,
    @NonNull BigDecimal upper
) {

  public ClassificationInterval {
    requireNonNull(lower, "classification interval lower bound cannot be null");
    requireNonNull(upper, "classification interval upper bound cannot be null");
    if (lower.compareTo(upper) > 0) {
      throw new DomainRuleViolationException(
          "classification interval lower bound cannot be greater than upper bound"
      );
    }
  }
}
