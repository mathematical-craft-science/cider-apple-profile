package org.craft.mcs.cap.core.cideranalysis.readiness.domain.planning;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ReadinessRuleSource;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Result of generic pH planning guidance.
 *
 * <p>The value is used by application read models to explain pH risks before a technologist chooses
 * yeast, SO2 strategy, acid adjustment, or another preparation action.
 */
public record PhPlanningGuidance(
    @NonNull PhPlanningGuidanceCode code,
    @NonNull String message,
    @Nullable BigDecimal value,
    @NonNull ReadinessRuleSource source
) {

  public PhPlanningGuidance {
    requireNonNull(code, "pH planning guidance code cannot be null");
    requireNonNull(message, "pH planning guidance message cannot be null");
    requireNonNull(source, "pH planning guidance source cannot be null");
    if (message.isBlank()) {
      throw new DomainRuleViolationException("pH planning guidance message cannot be blank");
    }
    if ((code == PhPlanningGuidanceCode.PH_MISSING) != (value == null)) {
      throw new DomainRuleViolationException("pH planning guidance value must match its code");
    }
  }
}
