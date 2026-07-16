package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizer;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
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
