package org.craft.mcs.cap.appleprofile.application.usecase;

import java.util.List;
import org.craft.mcs.cap.core.appleprofile.application.port.in.AppleVarietySugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.application.usecase.ListAppleVarietySugarAcidityZonePointsService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.BuiltInSugarAcidityZoneProfiles;
import org.craft.mcs.cap.shared.domain.sugaracidity.assessment.RectangleSugarAcidityZoneAssessor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ListAppleVarietySugarAcidityZonePointsServiceTest {

  @Test
  void returnsPlottablePointsAndUnplottableVarietiesWithMissingAxes() {
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new ListAppleVarietySugarAcidityZonePointsService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularGraphMetadata()
        )
    );
    final var plottable = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Kingston Black"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.062")
            )))
            .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.5")))
            .build()
    );
    final var unplottable = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build()
    );
    when(repository.findAll()).thenReturn(List.of(plottable, unplottable));

    final var batch = service.list();

    assertThat(batch.points())
        .extracting(AppleVarietySugarAcidityZoneAssessment::varietyId)
        .containsExactly(plottable.varietyId());
    assertThat(batch.unplottable()).hasSize(1);
    assertThat(batch.unplottable().getFirst().varietyId()).isEqualTo(unplottable.varietyId());
    assertThat(batch.unplottable().getFirst().capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
    assertThat(batch.unplottable().getFirst().capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.ACIDITY);
  }
}
