package org.craft.mcs.cap.shared.domain.chemical.capability;

import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.domain.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.domain.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.domain.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileCapabilityAssessorTest {

  private final ChemicalProfileAxisExtractor axisExtractor = new ChemicalProfileAxisExtractor();
  private final ChemicalProfileCapabilityAssessor assessor = new ChemicalProfileCapabilityAssessor();

  @Test
  void sugarAcidityPlotRequiresSugarAndAcidity() {
    final var assessment = assessor.assess(
        axisExtractor.extract(ChemicalProfile.builder()
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
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
            .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(
                SpecificGravityValue.of("1.052")
            )))
            .titratableAcidity(ExactMeasurement.of(TitratableAcidityValue.of("5.2")))
            .build()),
        ChemicalProfileCapability.SUGAR_ACIDITY_PLOT
    );

    assertThat(assessment.capable()).isTrue();
    assertThat(assessment.missingAxes()).isEmpty();
  }
}
