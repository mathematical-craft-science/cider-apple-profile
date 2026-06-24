package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * One check explaining whether a feedstock batch is usable for composition planning.
 */
public record FeedstockPlanningReadinessCheck(
    @NonNull FeedstockPlanningReadinessFactor factor,
    @NonNull FeedstockPlanningSignalSeverity severity,
    @NonNull String code,
    @NonNull String message
) {

  public FeedstockPlanningReadinessCheck {
    requireNonNull(factor, "feedstock planning planning readiness factor cannot be null");
    requireNonNull(severity, "feedstock planning signal severity cannot be null");
    requireNonNull(code, "feedstock planning planning readiness code cannot be null");
    requireNonNull(message, "feedstock planning planning readiness message cannot be null");
  }
}
