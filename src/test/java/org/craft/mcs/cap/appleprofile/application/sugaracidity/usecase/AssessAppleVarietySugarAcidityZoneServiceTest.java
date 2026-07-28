package org.craft.mcs.cap.appleprofile.application.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase.AssessAppleVarietySugarAcidityZoneService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;


class AssessAppleVarietySugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new AssessAppleVarietySugarAcidityZoneService(
        repository,
        projector(),
        predictor()
    );
    final var variety = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.050")
            )))
            .build()
    );
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

    final var result = service.assess(variety.varietyId());

    assertThat(result.evaluation().plottable()).isFalse();
    assertThat(result.evaluation().assessment()).isNull();
    assertThat(result.evaluation().capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH);
    assertThat(result.evaluation().capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.TITRATABLE_ACIDITY);
    assertThat(result.potentialAbv()).isNotNull();
  }

  private static ChemicalProfileAssessmentAdapter projector() {
    return chemicalProfileAssessmentAdapter();
  }
}
