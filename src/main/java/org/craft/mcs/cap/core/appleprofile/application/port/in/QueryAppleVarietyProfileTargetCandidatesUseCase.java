package org.craft.mcs.cap.core.appleprofile.application.port.in;

import org.jspecify.annotations.NonNull;


public interface QueryAppleVarietyProfileTargetCandidatesUseCase {

  @NonNull
  AppleVarietyProfileTargetCandidates query(@NonNull AppleVarietyProfileTargetCandidateQuery query);
}
