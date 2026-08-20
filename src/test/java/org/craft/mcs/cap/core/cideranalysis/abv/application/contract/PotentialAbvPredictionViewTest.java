package org.craft.mcs.cap.core.cideranalysis.abv.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class PotentialAbvPredictionViewTest {

  @Test
  void rejectsPredictedFinalGravityAboveOriginalGravity() {
    assertThatThrownBy(() -> new PotentialAbvPredictionView(
        SpecificGravityValue.of("1.000"),
        SpecificGravityValue.of("1.010"),
        FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT,
        AbvValue.of(BigDecimal.ZERO),
        AbvPredictionMethod.STANDARD_GRAVITY_DROP,
        PredictionConfidence.LOW
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("predicted final gravity must not exceed original gravity");
  }
}
