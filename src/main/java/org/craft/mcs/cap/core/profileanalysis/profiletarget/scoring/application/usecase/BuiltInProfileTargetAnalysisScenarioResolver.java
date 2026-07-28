package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Resolves built-in CAP analysis scenarios to explicit scoring defaults.
 */
@Component
public final class BuiltInProfileTargetAnalysisScenarioResolver
    implements ProfileTargetAnalysisScenarioResolver {

  @Override
  public @NonNull ProfileTargetScoringConfiguration resolve(
      @NonNull ProfileTargetAnalysisScenario scenario,
      @NonNull ProfileTargetObjectiveWeights weights
  ) {
    requireNonNull(scenario, "profile target analysis scenario cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");

    final var normalizerCode = switch (scenario) {
      case GRAPH_EXPLORATION -> ProfileAxisNormalizerCode.MIN_MAX;
      case BLENDING_PLANNING -> ProfileAxisNormalizerCode.EFFECTIVE_RANGE;
      case TARGET_ZONE_MATCHING -> ProfileAxisNormalizerCode.THRESHOLD_SEGMENT;
    };
    return new ProfileTargetScoringConfiguration(
        normalizerCode,
        ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
        weights
    );
  }
}
