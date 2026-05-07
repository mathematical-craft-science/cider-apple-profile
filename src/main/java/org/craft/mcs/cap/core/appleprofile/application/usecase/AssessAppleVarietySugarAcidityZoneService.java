package org.craft.mcs.cap.core.appleprofile.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AssessAppleVarietySugarAcidityZoneUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.classification.profile.sugaracidity.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class AssessAppleVarietySugarAcidityZoneService
    implements AssessAppleVarietySugarAcidityZoneUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull AppleVarietySugarAcidityZoneAssessment assess(
      @NonNull AppleVarietyId varietyId
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");

    final var variety = appleVarietyRepository.findById(varietyId)
        .orElseThrow(() -> new IllegalArgumentException("apple variety not found: " + varietyId.value()));
    final var assessment = assessor.assess(axisExtractor.extract(variety.chemicalProfile()))
        .orElseThrow(() -> new IllegalArgumentException(
            "apple variety chemical profile must have sugar and acidity axes: " + varietyId.value()
        ));

    return new AppleVarietySugarAcidityZoneAssessment(
        variety.varietyId(),
        variety.name(),
        assessment
    );
  }
}
