package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
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
