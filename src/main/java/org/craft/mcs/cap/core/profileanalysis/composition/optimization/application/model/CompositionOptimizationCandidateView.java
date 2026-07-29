package org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.model;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationCandidate;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Application view enriching a ranked composition candidate with its potential ABV prediction. */
public record CompositionOptimizationCandidateView(
    @NonNull CompositionOptimizationCandidate candidate,
    @NonNull PotentialAbvPrediction potentialAbv
) {

  public CompositionOptimizationCandidateView {
    requireNonNull(candidate, "composition optimization candidate cannot be null");
    requireNonNull(potentialAbv, "composition optimization candidate potential ABV cannot be null");
  }
}
