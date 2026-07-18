package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CalculateChemicalProfileCompositionResult(
    @NonNull ChemicalProfileComposition composition,
    @NonNull PotentialAbvPrediction potentialAbv,
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistance targetDistance
) {

  public CalculateChemicalProfileCompositionResult {
    requireNonNull(composition, "chemical profile composition cannot be null");
    requireNonNull(potentialAbv, "chemical profile composition potential ABV cannot be null");
  }

  public static @NonNull CalculateChemicalProfileCompositionResult withoutTargetDistance(
      @NonNull ChemicalProfileComposition composition,
      @NonNull PotentialAbvPrediction potentialAbv
  ) {
    return new CalculateChemicalProfileCompositionResult(composition, potentialAbv, null, null);
  }
}
