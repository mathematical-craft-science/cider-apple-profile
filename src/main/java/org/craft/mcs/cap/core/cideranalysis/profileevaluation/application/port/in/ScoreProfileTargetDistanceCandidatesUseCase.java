package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreProfileTargetDistanceCandidatesUseCase {

  @NonNull
  ProfileTargetDistanceCandidateScores score(
      @NonNull ScoreProfileTargetDistanceCandidatesCommand command
  );
}
