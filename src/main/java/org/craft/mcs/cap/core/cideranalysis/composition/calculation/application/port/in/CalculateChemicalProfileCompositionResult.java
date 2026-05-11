package org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileComposition;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetDistance;
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
