package org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.domain.fermentation.AbvPredictionMethod;
import org.craft.mcs.cap.shared.domain.fermentation.AbvValue;
import org.craft.mcs.cap.shared.domain.fermentation.FinalGravityPredictionSource;
import org.craft.mcs.cap.shared.domain.fermentation.PotentialAbvPrediction;
import org.craft.mcs.cap.shared.domain.fermentation.PredictionConfidence;
import org.craft.mcs.cap.shared.domain.sugar.SpecificGravityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class PotentialAbvPredictionRestMapperTest {

  @Test
  void mapsPredictionExplanationFields() {
    final var response = PotentialAbvPredictionRestMapper.toResponse(
        new PotentialAbvPrediction(
            SpecificGravityValue.of("1.052"),
            SpecificGravityValue.of("1.000"),
            FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT,
            AbvValue.of(new BigDecimal("6.825")),
            AbvPredictionMethod.STANDARD_GRAVITY_DROP,
            PredictionConfidence.LOW
        )
    );

    assertThat(response.originalGravity()).isEqualByComparingTo("1.052");
    assertThat(response.predictedFinalGravity()).isEqualByComparingTo("1.000");
    assertThat(response.predictedFinalGravitySource()).isEqualTo(FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT);
    assertThat(response.potentialAbv()).isEqualByComparingTo("6.825");
    assertThat(response.method()).isEqualTo(AbvPredictionMethod.STANDARD_GRAVITY_DROP);
    assertThat(response.confidence()).isEqualTo(PredictionConfidence.LOW);
  }
}
