package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Read-side planning readiness for using a feedstock batch in composition planning.
 *
 * <p>This is separate from fermentation-start readiness. A batch can be useful for planning while
 * still requiring YAN/pH checks before fermentation starts.
 */
public record FeedstockPlanningReadinessView(
    @NonNull FeedstockPlanningReadinessStatus status,
    @NonNull List<@NonNull FeedstockPlanningReadinessCheck> checks
) {

  public FeedstockPlanningReadinessView {
    requireNonNull(status, "feedstock planning readiness status cannot be null");
    checks = List.copyOf(requireNonNull(checks, "feedstock planning readiness checks cannot be null"));
  }
}
