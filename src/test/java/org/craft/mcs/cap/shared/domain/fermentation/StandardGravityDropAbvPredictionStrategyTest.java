package org.craft.mcs.cap.shared.domain.fermentation;

import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class StandardGravityDropAbvPredictionStrategyTest {

  private final StandardGravityDropAbvPredictionStrategy strategy =
      new StandardGravityDropAbvPredictionStrategy();

  @Test
  void predictsAbvFromGravityDrop() {
    final var abv = strategy.predict(new AbvPredictionInput(
        SpecificGravityValue.of("1.052"),
        SpecificGravityValue.of("1.000")
    ));

    assertThat(strategy.method()).isEqualTo(AbvPredictionMethod.STANDARD_GRAVITY_DROP);
    assertThat(abv.percent()).isEqualByComparingTo("6.825");
  }
}
