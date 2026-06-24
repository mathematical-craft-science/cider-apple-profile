package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreProfileTargetDistanceCandidatesUseCase {

  @NonNull
  ProfileTargetDistanceCandidateScores score(
      @NonNull ScoreProfileTargetDistanceCandidatesCommand command
  );
}
