package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.port.in;

import org.jspecify.annotations.NonNull;


public interface QueryAppleVarietyProfileTargetCandidatesUseCase {

  @NonNull
  AppleVarietyProfileTargetCandidates query(@NonNull AppleVarietyProfileTargetCandidateQuery query);
}
