package org.craft.mcs.cap.core.appleprofile.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietyNorthAmericanClassification;
import org.craft.mcs.cap.core.appleprofile.application.port.in.ClassifyAppleVarietyNorthAmericanUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityNotMetException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class ClassifyAppleVarietyNorthAmericanService
    implements ClassifyAppleVarietyNorthAmericanUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull NorthAmericanProfileCalculator calculator;

  @Override
  public @NonNull AppleVarietyNorthAmericanClassification classify(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new IllegalArgumentException("apple variety not found: " + varietyId.value()));

    final var axes = axisExtractor.extract(variety.chemicalProfile());
    final var capabilityAssessment = capabilityAssessor.assess(
        axes,
        ChemicalProfileCapability.NORTH_AMERICAN_PROFILE
    );
    if (!capabilityAssessment.capable()) {
      throw new ChemicalProfileCapabilityNotMetException(capabilityAssessment);
    }

    final var profile = calculator.calculate(axes);

    return new AppleVarietyNorthAmericanClassification(
        variety.varietyId(),
        variety.name(),
        profile
    );
  }
}
