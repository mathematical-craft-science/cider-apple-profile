package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import static java.util.Objects.requireNonNull;

import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition.Normalizer;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Authoritative calculation result with optional target assessment.
 */
public record CalculateCompositionResult(
    @NonNull CalculatedCompositionView composition,
    @NonNull FormulationPotentialAbvView potentialAbv,
    @Nullable Normalizer normalizerCode,
    @Nullable FormulationTargetDistanceView targetDistance
) {

  public CalculateCompositionResult {
    requireNonNull(composition, "composition cannot be null");
    requireNonNull(potentialAbv, "composition potential ABV cannot be null");
    if ((normalizerCode == null) != (targetDistance == null)) {
      throw new ApplicationValidationException(
          "composition normalizer and target distance must both be present or absent"
      );
    }
  }
}
