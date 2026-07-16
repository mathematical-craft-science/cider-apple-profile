package org.craft.mcs.cap.core.feedstock.application.currentprofile.model;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * One check explaining whether a feedstock batch is usable for composition planning.
 */
public record FeedstockPlanningReadinessCheck(
    @NonNull FeedstockPlanningReadinessFactor factor,
    @NonNull FeedstockCurrentProfileSignalSeverity severity,
    @NonNull FeedstockPlanningReadinessCheckCode code,
    @NonNull String message
) {

  public FeedstockPlanningReadinessCheck {
    requireNonNull(factor, "feedstock planning readiness factor cannot be null");
    requireNonNull(severity, "feedstock current profile signal severity cannot be null");
    requireNonNull(code, "feedstock planning readiness code cannot be null");
    requireNonNull(message, "feedstock planning readiness message cannot be null");
  }
}
