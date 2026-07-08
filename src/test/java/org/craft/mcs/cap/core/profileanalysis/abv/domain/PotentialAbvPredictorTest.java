package org.craft.mcs.cap.core.profileanalysis.abv.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class PotentialAbvPredictorTest {

  private final PotentialAbvPredictor predictor = new PotentialAbvPredictor(
      FixedFinalGravityPredictionStrategy.dryCiderDefault(),
      new StandardGravityDropAbvPredictionStrategy()
  );

  @Test
  void predictsPotentialAbvFromSugarConcentrationUsingDefaultFinalGravity() {
    final var prediction = predictor.predict(
        SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))
    );

    assertThat(prediction.originalGravity().asBigDecimal()).isEqualByComparingTo("1.052");
    assertThat(prediction.predictedFinalGravity().asBigDecimal()).isEqualByComparingTo("1.000");
    assertThat(prediction.predictedFinalGravitySource())
        .isEqualTo(FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT);
    assertThat(prediction.potentialAbv().percent()).isEqualByComparingTo("6.825");
    assertThat(prediction.method()).isEqualTo(AbvPredictionMethod.STANDARD_GRAVITY_DROP);
    assertThat(prediction.confidence()).isEqualTo(PredictionConfidence.LOW);
  }

  @Test
  void predictsPotentialAbvFromExplicitFinalGravity() {
    final var prediction = predictor.predict(
        SpecificGravityValue.of("1.052"),
        SpecificGravityValue.of("1.010")
    );

    assertThat(prediction.potentialAbv().percent()).isEqualByComparingTo("5.513");
  }

  @Test
  void usesFinalGravityPredictionStrategy() {
    final var strategy = new FixedFinalGravityPredictionStrategy(SpecificGravityValue.of("1.010"));
    final var prediction = new PotentialAbvPredictor(
        strategy,
        new StandardGravityDropAbvPredictionStrategy()
    ).predict(
        SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.052"))
    );

    assertThat(prediction.predictedFinalGravity().asBigDecimal()).isEqualByComparingTo("1.010");
    assertThat(prediction.predictedFinalGravitySource())
        .isEqualTo(FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT);
    assertThat(prediction.potentialAbv().percent()).isEqualByComparingTo("5.513");
    assertThat(prediction.confidence()).isEqualTo(PredictionConfidence.LOW);
  }

  @Test
  void rejectsFinalGravityGreaterThanOriginalGravity() {
    assertThatThrownBy(() -> predictor.predict(
        SpecificGravityValue.of("1.010"),
        SpecificGravityValue.of("1.020")
    )).isInstanceOf(DomainRuleViolationException.class);
  }
}
