package org.craft.mcs.cap.core.profileanalysis.profileaxis.domain;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileCapabilityAssessorTest {

  private final ChemicalProfileAxisExtractor axisExtractor = new ChemicalProfileAxisExtractor();
  private final ChemicalProfileCapabilityAssessor assessor = new ChemicalProfileCapabilityAssessor();

  @Test
  void sugarAcidityPlotRequiresSugarAndAcidity() {
    final var assessment = assessor.assess(
        axisExtractor.extract(ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.052")
            )))
            .build()),
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    assertThat(assessment.capable()).isFalse();
    assertThat(assessment.requiredAxes()).containsExactlyInAnyOrder(
        ChemicalAxis.SUGAR,
        ChemicalAxis.ACIDITY
    );
    assertThat(assessment.presentAxes()).containsExactly(ChemicalAxis.SUGAR);
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.ACIDITY);
  }

  @Test
  void sugarAcidityPlotIsCapableWhenSugarAndAcidityArePresent() {
    final var assessment = assessor.assess(
        axisExtractor.extract(ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.052")
            )))
            .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.2")))
            .build()),
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    assertThat(assessment.capable()).isTrue();
    assertThat(assessment.missingAxes()).isEmpty();
  }

  @Test
  void abvPredictionRequiresOnlySugar() {
    final var assessment = assessor.assess(
        axisExtractor.extract(ChemicalProfile.builder()
            .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.052")
            )))
            .build()),
        ChemicalProfileCapability.ABV_PREDICTION
    );

    assertThat(assessment.capable()).isTrue();
    assertThat(assessment.requiredAxes()).containsExactly(ChemicalAxis.SUGAR);
    assertThat(assessment.missingAxes()).isEmpty();
  }

  @Test
  void northAmericanProfileRequiresSugarAcidityAndTannin() {
    final var assessment = assessor.assess(
        axisExtractor.extract(sugarAndAcidityProfile()),
        ChemicalProfileCapability.NORTH_AMERICAN_PROFILE
    );

    assertThat(assessment.capable()).isFalse();
    assertThat(assessment.requiredAxes()).containsExactlyInAnyOrder(
        ChemicalAxis.SUGAR,
        ChemicalAxis.ACIDITY,
        ChemicalAxis.TANNIN
    );
    assertThat(assessment.presentAxes()).containsExactlyInAnyOrder(
        ChemicalAxis.SUGAR,
        ChemicalAxis.ACIDITY
    );
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.TANNIN);
  }

  @Test
  void singleVarietyCiderAssessmentRequiresSugarAcidityAndTannin() {
    final var assessment = assessor.assess(
        axisExtractor.extract(sugarAcidityAndTanninProfile()),
        ChemicalProfileCapability.SINGLE_VARIETY_CIDER_ASSESSMENT
    );

    assertThat(assessment.capable()).isTrue();
    assertThat(assessment.requiredAxes()).containsExactlyInAnyOrder(
        ChemicalAxis.SUGAR,
        ChemicalAxis.ACIDITY,
        ChemicalAxis.TANNIN
    );
    assertThat(assessment.missingAxes()).isEmpty();
  }

  private static ChemicalProfile sugarAndAcidityProfile() {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.2")))
        .build();
  }

  private static ChemicalProfile sugarAcidityAndTanninProfile() {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.2")))
        .tannin(PointCharacteristicValue.of(TanninValue.of("1500")))
        .build();
  }
}
