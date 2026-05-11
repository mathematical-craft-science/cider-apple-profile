package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import org.jspecify.annotations.NonNull;


public interface BlendProfileAxisNormalizerSelector {

  @NonNull
  BlendProfileAxisNormalizer select(@NonNull BlendProfileAxisNormalizerCode code);
}
