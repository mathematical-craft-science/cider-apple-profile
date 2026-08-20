package org.craft.mcs.cap.core.formulation.calculation.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FormulationPotentialAbvViewTest {

  @Test
  void rejectsPredictedFinalGravityAboveOriginalGravity() {
    assertThatThrownBy(() -> new FormulationPotentialAbvView(
        SpecificGravityValue.of("1.000"),
        SpecificGravityValue.of("1.010"),
        FormulationPotentialAbvView.FinalGravitySource.FIXED_DRY_CIDER_DEFAULT,
        AbvValue.of(BigDecimal.ZERO),
        FormulationPotentialAbvView.Method.STANDARD_GRAVITY_DROP,
        FormulationPotentialAbvView.Confidence.LOW
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("formulation predicted final gravity must not exceed original gravity");
  }
}
