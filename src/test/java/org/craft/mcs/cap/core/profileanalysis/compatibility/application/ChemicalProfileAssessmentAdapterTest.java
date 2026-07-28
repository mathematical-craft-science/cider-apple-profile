package org.craft.mcs.cap.core.profileanalysis.compatibility.application;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;


class ChemicalProfileAssessmentAdapterTest {

  private final ChemicalProfileAssessmentAdapter projector = chemicalProfileAssessmentAdapter();

  @Test
  void projectsCompleteChemicalProfileToSugarAcidityPointAndZoneAssessment() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.060")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("4.50")))
        .build();

    final var evaluation = projector.project(profile);

    assertThat(evaluation.plottable()).isTrue();
    assertThat(evaluation.capabilityAssessment().capability())
        .isEqualTo(ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH);
    assertThat(evaluation.assessment()).isNotNull();
    assertThat(evaluation.graphEvaluation().plottablePoint())
        .isPresent()
        .get()
        .extracting(
            point -> point.specificGravity().asBigDecimal(),
            point -> point.titratableAcidity().asBigDecimal()
        )
        .containsExactly(
            new BigDecimal("1.060"),
            new BigDecimal("4.50")
        );
    assertThat(evaluation.assessment())
        .extracting(JolicoeurSugarAcidityZoneAssessment::zone)
        .isEqualTo(JolicoeurSugarAcidityZone.PERFECT_APPLES);
  }

  @Test
  void returnsEmptyPointWhenChemicalProfileIsMissingRequiredAxis() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.050")
        )))
        .build();

    final var evaluation = projector.project(profile);

    assertThat(evaluation.plottable()).isFalse();
    assertThat(evaluation.assessment()).isNull();
    assertThat(evaluation.capabilityAssessment().missingAxes())
        .containsExactly(ChemicalAxis.TITRATABLE_ACIDITY);
  }

  @Test
  void exposesGraphPointThroughGraphEvaluation() {
    final var profile = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.060")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("4.50")))
        .build();

    assertThat(projector.project(profile).graphEvaluation().plottablePoint())
        .isPresent()
        .get()
        .extracting(point -> point.titratableAcidity().asBigDecimal())
        .isEqualTo(new BigDecimal("4.50"));
  }

  @Test
  void usesNorthAmericanTanninLevelForPointStylingWithoutChangingSugarAcidityZone() {
    final var mediumTanninProfile = profileWithTannin("2000");
    final var highTanninProfile = profileWithTannin("3000");

    final var mediumTanninEvaluation = projector.project(mediumTanninProfile);
    final var highTanninEvaluation = projector.project(highTanninProfile);

    assertThat(mediumTanninEvaluation)
        .returns(
            NorthAmericanTanninLevel.MEDIUM,
            ChemicalProfileAssessment::northAmericanTanninLevel
        );
    assertThat(highTanninEvaluation)
        .returns(
            NorthAmericanTanninLevel.HIGH,
            ChemicalProfileAssessment::northAmericanTanninLevel
        );
    assertThat(mediumTanninEvaluation.assessment())
        .isNotNull()
        .extracting(JolicoeurSugarAcidityZoneAssessment::zone)
        .isEqualTo(JolicoeurSugarAcidityZone.PERFECT_APPLES);
    assertThat(highTanninEvaluation.assessment())
        .isNotNull()
        .extracting(JolicoeurSugarAcidityZoneAssessment::zone)
        .isEqualTo(JolicoeurSugarAcidityZone.PERFECT_APPLES);
  }

  private static ChemicalProfile profileWithTannin(String tannin) {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.060")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.0")))
        .tannin(PointCharacteristicValue.of(TanninValue.of(tannin)))
        .build();
  }
}
