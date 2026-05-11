package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.jspecify.annotations.NonNull;


public interface ProfileAxisNormalizerSelector {

  @NonNull
  ProfileAxisNormalizer select(@NonNull ProfileAxisNormalizerCode code);
}
