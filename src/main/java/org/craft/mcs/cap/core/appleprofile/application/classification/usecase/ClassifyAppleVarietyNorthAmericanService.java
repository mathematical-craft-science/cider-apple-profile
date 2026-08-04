package org.craft.mcs.cap.core.appleprofile.application.classification.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.AppleVarietyNotFoundException;
import org.craft.mcs.cap.core.appleprofile.application.classification.AppleVarietyNorthAmericanProfileUnavailableException;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.AppleVarietyNorthAmericanClassification;
import org.craft.mcs.cap.core.appleprofile.application.classification.port.in.ClassifyAppleVarietyNorthAmericanUseCase;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.CiderAppleClassificationInput;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisInputAvailability;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for the North American profile projection of a catalog variety.
 *
 * <p>Unlike generic classification systems, the North American calculator is a reusable profile
 * calculation component that can later participate in other algorithms. This service therefore
 * performs the use-case specific steps around it: load the variety and require the typed
 * characteristics needed by the North American profile.
 */
@Component
@RequiredArgsConstructor
public final class ClassifyAppleVarietyNorthAmericanService
    implements ClassifyAppleVarietyNorthAmericanUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull NorthAmericanProfileCalculator calculator;

  @Override
  public @NonNull AppleVarietyNorthAmericanClassification classify(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new AppleVarietyNotFoundException(varietyId));
    final var referenceProfile = variety.referenceProfile();
    if (referenceProfile.specificGravity() == null
        || referenceProfile.titratableAcidity() == null
        || referenceProfile.tannin() == null) {
      throw new AppleVarietyNorthAmericanProfileUnavailableException(
          varietyId,
          new ProfileAnalysisInputAvailability(
              referenceProfile.specificGravity() != null,
              referenceProfile.titratableAcidity() != null,
              referenceProfile.tannin() != null
          )
      );
    }

    final var profile = calculator.calculate(
        new CiderAppleClassificationInput(
            referenceProfile.specificGravity(),
            referenceProfile.titratableAcidity(),
            referenceProfile.tannin()
        )
    );

    return new AppleVarietyNorthAmericanClassification(
        variety.varietyId(),
        variety.name(),
        profile
    );
  }
}
