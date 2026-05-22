package org.craft.mcs.cap.core.cideranalysis.abv.domain;

import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class FixedFinalGravityPredictionStrategyTest {

  @Test
  void returnsFixedDryCiderDefaultWithLowConfidence() {
    final var prediction = FixedFinalGravityPredictionStrategy.dryCiderDefault()
        .predictFinalGravity(new FinalGravityPredictionInput(SpecificGravityValue.of("1.052")));

    assertThat(prediction.finalGravity().asBigDecimal()).isEqualByComparingTo("1.000");
    assertThat(prediction.source()).isEqualTo(FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT);
    assertThat(prediction.confidence()).isEqualTo(PredictionConfidence.LOW);
  }
}
