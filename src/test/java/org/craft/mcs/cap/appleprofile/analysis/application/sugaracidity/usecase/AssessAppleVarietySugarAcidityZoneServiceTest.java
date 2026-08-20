package org.craft.mcs.cap.appleprofile.analysis.application.sugaracidity.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.appleGraphAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.appleClassificationAnalyzer;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.applePotentialAbvAnalyzer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.port.out.AppleVarietyAnalysisProfileProvider;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView.AcidityLevel;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView.SugarLevel;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView.MissingCharacteristic;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.service.AppleVarietyGraphAnalysisProjector;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.usecase.AssessAppleVarietySugarAcidityZoneService;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
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
            .returns(SugarLevel.MEDIUM, AppleVarietyNorthAmericanAssessmentView::sugar)
            .returns(null, AppleVarietyNorthAmericanAssessmentView::acidity)
            .returns(null, AppleVarietyNorthAmericanAssessmentView::tannin))
        .satisfies(view -> assertThat(view.graphEvaluation().missingCharacteristics())
            .containsExactly(MissingCharacteristic.TITRATABLE_ACIDITY));
  }

  @Test
  void preservesVeryHighNorthAmericanSugarAndAcidityLevels() {
    final var profileProvider = mock(AppleVarietyAnalysisProfileProvider.class);
    final var service = new AssessAppleVarietySugarAcidityZoneService(profileProvider, projector());
    final var variety = new AppleVarietyAnalysisProfile(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Concentrated Profile"),
        PointCharacteristicValue.of(SpecificGravityValue.of("1.070")),
        PointCharacteristicValue.of(TitratableAcidityValue.of("11.0")),
        null,
        null
    );
    when(profileProvider.get(variety.varietyId())).thenReturn(variety);

    final var assessment = service.assess(variety.varietyId()).northAmericanAssessment();

    assertThat(assessment)
        .isNotNull()
        .returns(SugarLevel.VERY_HIGH, AppleVarietyNorthAmericanAssessmentView::sugar)
        .returns(AcidityLevel.VERY_HIGH, AppleVarietyNorthAmericanAssessmentView::acidity);
  }

  private static AppleVarietyGraphAnalysisProjector projector() {
    return new AppleVarietyGraphAnalysisProjector(
        appleGraphAnalyzer(),
        appleClassificationAnalyzer(),
        applePotentialAbvAnalyzer()
    );
  }
}
