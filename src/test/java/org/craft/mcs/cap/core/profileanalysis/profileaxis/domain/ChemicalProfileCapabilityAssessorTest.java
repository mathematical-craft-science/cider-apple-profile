package org.craft.mcs.cap.core.profileanalysis.profileaxis.domain;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
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
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH
    );

    assertThat(assessment.capable()).isFalse();
    assertThat(assessment.requiredAxes()).containsExactlyInAnyOrder(
        ChemicalAxis.SPECIFIC_GRAVITY,
        ChemicalAxis.TITRATABLE_ACIDITY
    );
    assertThat(assessment.presentAxes()).containsExactly(ChemicalAxis.SPECIFIC_GRAVITY);
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.TITRATABLE_ACIDITY);
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
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH
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
    assertThat(assessment.requiredAxes()).containsExactly(ChemicalAxis.SPECIFIC_GRAVITY);
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
        ChemicalAxis.SPECIFIC_GRAVITY,
        ChemicalAxis.TITRATABLE_ACIDITY,
        ChemicalAxis.TANNIN
    );
    assertThat(assessment.presentAxes()).containsExactlyInAnyOrder(
        ChemicalAxis.SPECIFIC_GRAVITY,
        ChemicalAxis.TITRATABLE_ACIDITY
    );
    assertThat(assessment.missingAxes()).containsExactly(ChemicalAxis.TANNIN);
  }

  private static ChemicalProfile sugarAndAcidityProfile() {
    return ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of("1.052")
        )))
        .titratableAcidity(PointCharacteristicValue.of(TitratableAcidityValue.of("5.2")))
        .build();
  }
}
