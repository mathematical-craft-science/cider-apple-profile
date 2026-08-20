package org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Feedstock-owned pH planning guidance facts. */
public record FeedstockPhPlanningGuidanceView(
    @NonNull Code code,
    @NonNull String message,
    @Nullable BigDecimal value
) {

  public FeedstockPhPlanningGuidanceView {
    requireNonNull(code, "pH planning guidance code cannot be null");
    requireNonNull(message, "pH planning guidance message cannot be null");
  }

  public enum Code {
    PH_PLANNING_IDEAL,
    PH_PLANNING_LOW,
    PH_PLANNING_HIGH,
    PH_MISSING
  }
}
