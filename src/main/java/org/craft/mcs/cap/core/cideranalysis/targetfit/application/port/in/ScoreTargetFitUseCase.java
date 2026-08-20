package org.craft.mcs.cap.core.cideranalysis.targetfit.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.targetfit.application.contract.TargetFitCandidateScoresView;
import org.jspecify.annotations.NonNull;


/**
 * Scores immutable characteristic snapshots against one target without persistence or locking.
 */
public interface ScoreTargetFitUseCase {

  @NonNull
  TargetFitCandidateScoresView score(@NonNull TargetFitCommand command);
}
