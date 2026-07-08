package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import java.math.BigDecimal;
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
    @NonNull PhRuleSource source
) {

  public PhPlanningGuidance {
    requireNonNull(code, "pH planning guidance code cannot be null");
    requireNonNull(message, "pH planning guidance message cannot be null");
    requireNonNull(source, "pH planning guidance source cannot be null");
  }
}
