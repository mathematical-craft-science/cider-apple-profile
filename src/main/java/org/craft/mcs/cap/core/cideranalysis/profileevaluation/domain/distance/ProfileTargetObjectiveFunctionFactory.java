package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.jspecify.annotations.NonNull;


/**
 * Creates objective function instances after the application has selected metric, normalizer and weights.
 */
public interface ProfileTargetObjectiveFunctionFactory {

  @NonNull
  ProfileTargetObjectiveFunction create(
      @NonNull ProfileTargetObjectiveFunctionCode code,
      @NonNull ProfileAxisNormalizer normalizer,
      @NonNull ProfileTargetObjectiveWeights weights
  );
}
