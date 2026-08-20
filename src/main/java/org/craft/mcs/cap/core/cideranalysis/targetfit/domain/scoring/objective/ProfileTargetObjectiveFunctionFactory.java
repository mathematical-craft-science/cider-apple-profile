package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.objective;

import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization.ProfileAxisNormalizer;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileTargetObjectiveWeights;
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
