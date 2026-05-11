package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.jspecify.annotations.NonNull;


public interface ScoreBlendProfileDistanceCandidatesUseCase {

  @NonNull
  BlendProfileDistanceCandidateScores score(
      @NonNull ScoreBlendProfileDistanceCandidatesCommand command
  );
}
