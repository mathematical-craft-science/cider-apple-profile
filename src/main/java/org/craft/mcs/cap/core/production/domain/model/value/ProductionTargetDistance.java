package org.craft.mcs.cap.core.production.domain.model.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isNegative;


/**
 * Distance from the accepted production profile to a previously selected target.
 *
 * <p>The value is calculated before production creation by composition/scoring use cases. Zero
 * means that the accepted production profile is inside the target area; higher values mean a larger
 * normalized deviation. The production context stores it only as accepted decision metadata.
 */
@ValueObject
public record ProductionTargetDistance(@NonNull BigDecimal value) {

  public ProductionTargetDistance {
    requireNonNull(value, "production target distance cannot be null");
    if (isNegative(value)) {
      throw new DomainRuleViolationException("production target distance cannot be negative");
    }
  }

  public static @NonNull ProductionTargetDistance of(@NonNull BigDecimal value) {
    return new ProductionTargetDistance(value);
  }
}
