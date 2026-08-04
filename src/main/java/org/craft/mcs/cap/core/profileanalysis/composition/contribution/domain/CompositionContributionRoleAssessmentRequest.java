package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Input for target-aware contribution role assessment.
 */
public record CompositionContributionRoleAssessmentRequest(
    @NonNull ProfileTargetDistance targetDistance
) {

  public CompositionContributionRoleAssessmentRequest {
    requireNonNull(targetDistance, "composition contribution target distance cannot be null");
  }
}
