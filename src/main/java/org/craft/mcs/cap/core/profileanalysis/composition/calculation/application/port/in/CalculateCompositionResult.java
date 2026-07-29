package org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in;

import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CalculatedComposition;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.assessment.ProfileTargetDistance;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Authoritative calculation result with optional, internally consistent target assessment metadata. */
public record CalculateCompositionResult(
    @NonNull CalculatedComposition composition,
    @NonNull PotentialAbvPrediction potentialAbv,
    @Nullable ProfileAxisNormalizerCode normalizerCode,
    @Nullable ProfileTargetDistance targetDistance
) {

  public CalculateCompositionResult {
    requireNonNull(composition, "composition cannot be null");
    requireNonNull(potentialAbv, "composition potential ABV cannot be null");
    if ((normalizerCode == null) != (targetDistance == null)) {
      throw new ApplicationValidationException(
          "composition normalizer and target distance must either both be present or both be absent"
      );
    }
  }

  public static @NonNull CalculateCompositionResult withoutTargetDistance(
      @NonNull CalculatedComposition composition,
      @NonNull PotentialAbvPrediction potentialAbv
  ) {
    return new CalculateCompositionResult(composition, potentialAbv, null, null);
  }
}
