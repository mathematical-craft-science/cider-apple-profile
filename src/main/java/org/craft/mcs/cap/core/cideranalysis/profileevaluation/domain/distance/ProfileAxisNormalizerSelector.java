package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.jspecify.annotations.NonNull;


/**
 * Selects the normalizer used by distance/objective calculations.
 *
 * <p>This selector is the normalization extension point shared by graph scoring, feedstock target
 * scoring and composition optimization. It keeps objective functions independent from how an
 * application scenario chooses axis scaling.
 */
public interface ProfileAxisNormalizerSelector {

  @NonNull
  ProfileAxisNormalizer select(@NonNull ProfileAxisNormalizerCode code);
}
