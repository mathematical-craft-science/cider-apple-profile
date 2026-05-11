package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.usecase;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.jspecify.annotations.NonNull;


public interface ProfileTargetScoringPurposeNormalizerPolicy {

  @NonNull
  ProfileAxisNormalizerCode normalizerFor(@NonNull ProfileTargetScoringPurpose purpose);
}
