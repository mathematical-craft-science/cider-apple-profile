package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


public interface BlendProfileScoringPurposeNormalizerPolicy {

  @NonNull
  BlendProfileAxisNormalizerCode normalizerFor(@NonNull BlendProfileScoringPurpose purpose);
}
