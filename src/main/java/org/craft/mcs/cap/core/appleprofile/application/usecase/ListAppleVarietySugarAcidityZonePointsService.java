package org.craft.mcs.cap.core.appleprofile.application.usecase;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZonePointBatch;
import org.craft.mcs.cap.core.appleprofile.application.port.in.ListAppleVarietySugarAcidityZonePointsUseCase;
import org.craft.mcs.cap.core.appleprofile.application.port.in.UnplottableAppleVarietySugarAcidityZonePoint;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessment;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
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
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor assessor;

  @Override
  public @NonNull AppleVarietySugarAcidityZonePointBatch list() {
    final List<@NonNull AppleVarietySugarAcidityZoneAssessment> points = new ArrayList<>();
    final List<@NonNull UnplottableAppleVarietySugarAcidityZonePoint> unplottable = new ArrayList<>();

    for (final var variety : appleVarietyRepository.findAll()) {
      final var axes = axisExtractor.extract(variety.chemicalProfile());
      final var capability = capabilityAssessor.assess(
          axes,
          ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
      );

      if (capability.capable()) {
        assessor.assess(axes)
            .map(assessment -> toAssessment(variety, assessment))
            .ifPresent(points::add);
      } else {
        unplottable.add(toUnplottable(variety, capability));
      }
    }

    return new AppleVarietySugarAcidityZonePointBatch(points, unplottable);
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

  private static @NonNull UnplottableAppleVarietySugarAcidityZonePoint toUnplottable(
      @NonNull AppleVariety variety,
      @NonNull ChemicalProfileCapabilityAssessment capabilityAssessment
  ) {
    return new UnplottableAppleVarietySugarAcidityZonePoint(
        variety.varietyId(),
        variety.name(),
        capabilityAssessment
    );
  }
}
