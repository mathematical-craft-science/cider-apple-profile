package org.craft.mcs.cap.core.cideranalysis.composition.contribution.domain;

import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Input for target-aware contribution role assessment.
 */
public record CompositionContributionRoleAssessmentRequest(
    @NonNull ChemicalProfileAxes sourceAxes,
    @NonNull ProfileTargetObjective targetObjective
) {

  public CompositionContributionRoleAssessmentRequest {
    requireNonNull(sourceAxes, "composition contribution source axes cannot be null");
    requireNonNull(targetObjective, "composition contribution target objective cannot be null");
  }
}
