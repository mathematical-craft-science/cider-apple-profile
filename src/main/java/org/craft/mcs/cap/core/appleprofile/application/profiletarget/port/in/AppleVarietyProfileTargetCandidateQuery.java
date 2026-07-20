package org.craft.mcs.cap.core.appleprofile.application.profiletarget.port.in;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.port.in.AppleVarietyPointFilters;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record AppleVarietyProfileTargetCandidateQuery(
    @NonNull AppleVarietyPointFilters filters,
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull ProfileTargetObjective target,
    @NonNull ProfileTargetObjectiveWeights weights,
    int limit
) {

  public AppleVarietyProfileTargetCandidateQuery {
    requireNonNull(filters, "apple variety profile target candidate filters cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(target, "profile target objective cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");

    if (limit < 1 || limit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "apple variety profile target candidate limit must be between 1 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
