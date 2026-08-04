package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Explicit mathematical configuration used to score a profile against a target.
 *
 * <p>The configuration is independent of user-facing analysis scenarios. Application services
 * may resolve scenario defaults into this value before invoking scoring or composition logic.
 */
public record ProfileTargetScoringConfiguration(
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    @NonNull ProfileTargetObjectiveWeights weights
) {

  public ProfileTargetScoringConfiguration {
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");
  }
}
