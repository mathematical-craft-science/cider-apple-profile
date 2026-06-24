package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import org.jspecify.annotations.NonNull;


/**
 * Strategy for assigning source contribution roles relative to a selected target.
 */
public interface CompositionContributionRoleAssessor {

  @NonNull
  CompositionContributionRoleAssessment assess(@NonNull CompositionContributionRoleAssessmentRequest request);
}
