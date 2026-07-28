package org.craft.mcs.cap.core.appleprofile.application.abv.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.application.abv.port.in.AppleVarietyPotentialAbvPrediction;
import org.craft.mcs.cap.core.appleprofile.application.abv.port.in.PredictAppleVarietyPotentialAbvUseCase;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityNotMetException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for potential ABV prediction from a catalog apple variety profile.
 *
 * <p>The service treats apple variety sugar as a reference/typical original gravity and delegates
 * the ABV math to the generic profile-analysis predictor. It does not imply that the final cider ABV
 * is fixed: real production batches should use current feedstock or production measurements.
 */
@Component
@RequiredArgsConstructor
public final class PredictAppleVarietyPotentialAbvService
    implements PredictAppleVarietyPotentialAbvUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull PotentialAbvPredictor predictor;

  @Override
  public @NonNull AppleVarietyPotentialAbvPrediction predict(@NonNull AppleVarietyId varietyId) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new AppleVarietyNotFoundException(varietyId));
    final var axes = axisExtractor.extract(variety.chemicalProfile());
    final var capability = capabilityAssessor.assess(axes, ChemicalProfileCapability.ABV_PREDICTION);

    if (!capability.capable()) {
      throw new ChemicalProfileCapabilityNotMetException(capability);
    }

    final var specificGravity = axes.valueOf(ChemicalAxis.SPECIFIC_GRAVITY)
        .orElseThrow(() -> new ChemicalProfileCapabilityNotMetException(capability));

    return new AppleVarietyPotentialAbvPrediction(
        variety.varietyId(),
        variety.name(),
        specificGravity.valueKind(),
        predictor.predict(SpecificGravityValue.of(specificGravity.value()))
    );
  }
}
