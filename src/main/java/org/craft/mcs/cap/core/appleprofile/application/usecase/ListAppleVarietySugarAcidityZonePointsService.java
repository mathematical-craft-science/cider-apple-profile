package org.craft.mcs.cap.core.appleprofile.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.appleprofile.application.port.in.ListAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.SugarAcidityZoneAssessor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public final class ListAppleVarietySugarAcidityZonePointsService
    implements ListAppleVarietySugarAcidityZonePointsUseCase {

  private final @NonNull AppleVarietyRepository appleVarietyRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull List<@NonNull AppleVarietySugarAcidityZoneAssessment> list() {
    return appleVarietyRepository.findAll()
        .stream()
        .flatMap(variety -> assessor.assess(axisExtractor.extract(variety.chemicalProfile()))
            .map(assessment -> toAssessment(variety, assessment))
            .stream())
        .toList();
  }

  private static @NonNull AppleVarietySugarAcidityZoneAssessment toAssessment(
      @NonNull AppleVariety variety,
      @NonNull SugarAcidityZoneAssessment assessment
  ) {
    return new AppleVarietySugarAcidityZoneAssessment(
        variety.varietyId(),
        variety.name(),
        assessment
    );
  }
}
