package org.craft.mcs.cap.appleprofile.application.usecase;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.application.usecase.AssessAppleVarietySugarAcidityZoneService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.domain.chemical.capability.ChemicalProfileCapabilityAssessor;
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


class AssessAppleVarietySugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new AssessAppleVarietySugarAcidityZoneService(
        repository,
        new ChemicalProfileAxisExtractor(),
        new ChemicalProfileCapabilityAssessor(),
        new RectangleSugarAcidityZoneAssessor(
            BuiltInSugarAcidityZoneProfiles.thresholds(),
            BuiltInSugarAcidityZoneProfiles.rectangularGraphMetadata()
        )
    );
    final var variety = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build()
    );
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

    final var result = service.assess(variety.varietyId());

    assertThat(result.plottable()).isFalse();
    assertThat(result.assessment()).isNull();
    assertThat(result.capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_PLOT);
    assertThat(result.capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.ACIDITY);
  }
}
