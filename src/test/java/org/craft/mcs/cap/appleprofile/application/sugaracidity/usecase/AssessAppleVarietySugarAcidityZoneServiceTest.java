package org.craft.mcs.cap.appleprofile.application.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures.graphEvaluator;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.craft.mcs.cap.core.appleprofile.application.catalog.port.out.AppleVarietyRepository;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.usecase.AssessAppleVarietySugarAcidityZoneService;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVariety;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;


class AssessAppleVarietySugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var repository = mock(AppleVarietyRepository.class);
    final var service = new AssessAppleVarietySugarAcidityZoneService(
        repository,
        graphEvaluator(),
        northAmericanProfileCalculator(),
        predictor()
    );
    final var variety = AppleVariety.create(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        new AppleVarietyReferenceProfile(
            PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
            null,
            null,
            null
        )
    );
    when(repository.findById(variety.varietyId())).thenReturn(Optional.of(variety));

    final var result = service.assess(variety.varietyId());

    assertThat(result.graphEvaluation().plottable()).isFalse();
    assertThat(result.graphEvaluation().zoneAssessment()).isNull();
    assertThat(result.graphEvaluation().missingCharacteristics())
        .containsExactly(GraphMissingCharacteristic.TITRATABLE_ACIDITY);
    assertThat(result.potentialAbv()).isNotNull();
  }
}
