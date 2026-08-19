package org.craft.mcs.cap.appleprofile.analysis.application.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures.graphEvaluator;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.service.AppleVarietyGraphAnalysisProjector;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.usecase.AssessAppleVarietySugarAcidityZoneService;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanSugarLevel;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.junit.jupiter.api.Test;


class AssessAppleVarietySugarAcidityZoneServiceTest {

  @Test
  void returnsUnplottableResultForIncompleteProfile() {
    final var profileProvider = mock(AppleVarietyAnalysisProfileProvider.class);
    final var service = new AssessAppleVarietySugarAcidityZoneService(
        profileProvider,
        projector()
    );
    final var variety = new AppleVarietyAnalysisProfile(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Partial Profile"),
        PointCharacteristicValue.of(SpecificGravityValue.of("1.050")),
        null,
        null,
        null
    );
    when(profileProvider.get(variety.varietyId())).thenReturn(variety);

    final var result = service.assess(variety.varietyId());

    assertThat(result)
        .returns(false, view -> view.graphEvaluation().plottable())
        .returns(null, view -> view.graphEvaluation().zoneAssessment())
        .matches(view -> view.potentialAbv() != null)
        .satisfies(view -> assertThat(view.northAmericanAssessment())
            .isNotNull()
            .returns(NorthAmericanSugarLevel.MEDIUM, AppleVarietyNorthAmericanAssessmentView::sugar)
            .returns(null, AppleVarietyNorthAmericanAssessmentView::acidity)
            .returns(null, AppleVarietyNorthAmericanAssessmentView::tannin))
        .satisfies(view -> assertThat(view.graphEvaluation().missingCharacteristics())
            .containsExactly(GraphMissingCharacteristic.TITRATABLE_ACIDITY));
  }

  private static AppleVarietyGraphAnalysisProjector projector() {
    return new AppleVarietyGraphAnalysisProjector(
        graphEvaluator(),
        northAmericanProfileCalculator(),
        predictor()
    );
  }
}
