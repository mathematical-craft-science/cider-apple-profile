package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileEvaluationAxis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * One typed semantic fact explaining target-aware contribution evidence.
 *
 * <p>Numerical evidence, primary role, and per-axis role remain in the contribution-role assessment.
 * This object only adds interpretation facts needed for frontend rendering.
 */
public record FeedstockPlanningRoleEvidenceFact(
    @NonNull ProfileEvaluationAxis axis,
    @NonNull FeedstockPlanningRoleEvidenceDirection direction,
    @NonNull FeedstockPlanningRoleEvidenceEffect effect,
    @NonNull FeedstockPlanningRoleEvidenceSeverity severity
) {

  public FeedstockPlanningRoleEvidenceFact {
    requireNonNull(axis, "feedstock planning role evidence axis cannot be null");
    requireNonNull(direction, "feedstock planning role evidence direction cannot be null");
    requireNonNull(effect, "feedstock planning role evidence effect cannot be null");
    requireNonNull(severity, "feedstock planning role evidence severity cannot be null");
  }
}
