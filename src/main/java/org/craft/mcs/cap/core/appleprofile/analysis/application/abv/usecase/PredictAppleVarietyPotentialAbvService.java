package org.craft.mcs.cap.core.appleprofile.analysis.application.abv.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.AppleVarietyPotentialAbvUnavailableException;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.in.AppleVarietyPotentialAbvPrediction;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.in.PredictAppleVarietyPotentialAbvUseCase;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.out.ApplePotentialAbvAnalyzer;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Application service for potential ABV prediction from a catalog apple variety profile.
 *
 * <p>The service treats apple variety sugar as a reference/typical original gravity and delegates
 * the ABV math to the CiderAnalysis predictor. It does not imply that the final cider ABV
 * is fixed: real production batches should use current feedstock or production measurements.
 */
@Component
@RequiredArgsConstructor
public final class PredictAppleVarietyPotentialAbvService
    implements PredictAppleVarietyPotentialAbvUseCase {

  private final @NonNull AppleVarietyAnalysisProfileProvider profileProvider;
  private final @NonNull ApplePotentialAbvAnalyzer potentialAbvAnalyzer;

  @Override
  public @NonNull AppleVarietyPotentialAbvPrediction predict(@NonNull AppleVarietyId varietyId) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = profileProvider.get(varietyId);
    final var specificGravity = variety.specificGravity();
    if (specificGravity == null) {
      throw new AppleVarietyPotentialAbvUnavailableException(
          varietyId
      );
    }

    return new AppleVarietyPotentialAbvPrediction(
        variety.varietyId(),
        variety.name(),
        specificGravity,
        potentialAbvAnalyzer.predict(SpecificGravityValue.of(midpoint(specificGravity)))
    );
  }
}
