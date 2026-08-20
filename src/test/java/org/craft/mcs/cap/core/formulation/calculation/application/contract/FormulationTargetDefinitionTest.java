package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FormulationTargetDefinitionTest {

  @Test
  void rejectsNonPositiveScoringWeights() {
    assertThatThrownBy(() -> new FormulationTargetDefinition.Weights(
        BigDecimal.ZERO,
        BigDecimal.ONE,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("formulation target weights must be positive");
  }

  @Test
  void rejectsNonPositiveOptionalTanninWeight() {
    assertThatThrownBy(() -> new FormulationTargetDefinition.Weights(
        BigDecimal.ONE,
        BigDecimal.ONE,
        BigDecimal.ZERO
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("formulation target weights must be positive");
  }

  @Test
  void rejectsHalfOpenTanninTarget() {
    assertThatThrownBy(() -> new FormulationTargetDefinition(
        new FormulationTargetDefinition.PredefinedTarget(FormulationTargetDefinition.Zone.PERFECT_APPLES),
        MeasuredValueRange.closedOpen(TanninValue.of("800"), TanninValue.of("1800")),
        new FormulationTargetDefinition.ScoringConfiguration(
            FormulationTargetDefinition.Normalizer.EFFECTIVE_RANGE,
            FormulationTargetDefinition.ObjectiveFunction.DISTANCE_TO_TARGET_AREA,
            new FormulationTargetDefinition.Weights(BigDecimal.ONE, BigDecimal.ONE, null)
        )
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("formulation tannin target must include its maximum");
  }
}
