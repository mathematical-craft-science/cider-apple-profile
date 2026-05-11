package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileComposition;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * One optimized candidate returned by an optimization strategy.
 *
 * <p>The candidate contains the calculated composition and its distance to the target. The
 * composition includes selected sources and generated weights; the distance explains how close this
 * candidate is according to the selected objective function and normalizer.
 */
public record ChemicalProfileCompositionOptimizationCandidate(
    @NonNull ChemicalProfileComposition composition,
    @NonNull ProfileTargetDistance distance
) {

  public ChemicalProfileCompositionOptimizationCandidate {
    requireNonNull(composition, "chemical profile composition cannot be null");
    requireNonNull(distance, "profile target distance cannot be null");
  }
}
