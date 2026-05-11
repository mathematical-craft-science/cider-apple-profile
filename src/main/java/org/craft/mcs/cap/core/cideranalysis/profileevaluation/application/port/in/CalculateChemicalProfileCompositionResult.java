package org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileComposition;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetDistance;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record CalculateChemicalProfileCompositionResult(
    @NonNull ChemicalProfileComposition composition,
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistance targetDistance
) {

  public CalculateChemicalProfileCompositionResult {
    requireNonNull(composition, "chemical profile composition cannot be null");
  }

  public static @NonNull CalculateChemicalProfileCompositionResult withoutTargetDistance(
      @NonNull ChemicalProfileComposition composition
  ) {
    return new CalculateChemicalProfileCompositionResult(composition, null, null);
  }
}
