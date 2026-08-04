package org.craft.mcs.cap.core.profileanalysis.composition.contribution.domain;

import org.jspecify.annotations.NonNull;


/**
 * Strategy for assigning source contribution roles relative to a selected target.
 */
public interface CompositionContributionRoleAssessor {

  /**
   * Interprets a scored source against its target.
   *
   * @return a non-empty role assessment consistent with the scored target position
   */
  @NonNull
  CompositionContributionRoleAssessment assess(
      @NonNull CompositionContributionRoleAssessmentRequest request
  );
}
