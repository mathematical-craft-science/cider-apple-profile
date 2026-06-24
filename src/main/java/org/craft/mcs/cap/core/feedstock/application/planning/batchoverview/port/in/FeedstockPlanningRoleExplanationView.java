package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Compact explanation of a target-aware source role for a planning row.
 */
public record FeedstockPlanningRoleExplanationView(
    @NonNull String summary,
    @NonNull List<@NonNull String> reasons
) {

  public FeedstockPlanningRoleExplanationView {
    requireNonNull(summary, "feedstock planning role explanation summary cannot be null");
    reasons = List.copyOf(requireNonNull(reasons, "feedstock planning role explanation reasons cannot be null"));
  }
}
