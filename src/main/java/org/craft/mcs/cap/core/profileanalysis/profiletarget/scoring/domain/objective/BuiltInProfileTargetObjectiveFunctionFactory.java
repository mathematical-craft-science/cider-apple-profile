package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizer;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.jspecify.annotations.NonNull;
import static java.util.Objects.requireNonNull;


/**
 * Creates built-in profile target objective function strategies.
 *
 * <p>The factory is used by application services instead of constructing objective functions
 * directly. This keeps algorithm selection visible in commands and responses while isolating the
 * concrete mathematical implementations inside the distance domain package.
 */
public final class BuiltInProfileTargetObjectiveFunctionFactory implements ProfileTargetObjectiveFunctionFactory {

  @Override
  public @NonNull ProfileTargetObjectiveFunction create(
      @NonNull ProfileTargetObjectiveFunctionCode code,
      @NonNull ProfileAxisNormalizer normalizer,
      @NonNull ProfileTargetObjectiveWeights weights
  ) {
    requireNonNull(code, "profile target objective function code cannot be null");
    requireNonNull(normalizer, "profile axis normalizer cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");

    return switch (code) {
      case DISTANCE_TO_TARGET_AREA -> TargetAreaProfileTargetObjectiveFunction.euclidean(normalizer, weights);
      case MANHATTAN_DISTANCE_TO_TARGET_AREA ->
          TargetAreaProfileTargetObjectiveFunction.manhattan(normalizer, weights);
    };
  }
}
