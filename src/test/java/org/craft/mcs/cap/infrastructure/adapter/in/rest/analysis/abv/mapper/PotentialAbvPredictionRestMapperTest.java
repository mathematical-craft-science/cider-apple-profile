package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.AbvPredictionMethod;
import org.craft.mcs.cap.shared.kernel.measurement.value.AbvValue;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PredictionConfidence;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
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

    assertThat(response.originalGravity().value()).isEqualByComparingTo("1.052");
    assertThat(response.originalGravity().unit()).isEqualTo("SG");
    assertThat(response.predictedFinalGravity().value()).isEqualByComparingTo("1.000");
    assertThat(response.predictedFinalGravity().unit()).isEqualTo("SG");
    assertThat(response.predictedFinalGravitySource()).isEqualTo(FinalGravityPredictionSource.FIXED_DRY_CIDER_DEFAULT);
    assertThat(response.potentialAbv().value()).isEqualByComparingTo("6.825");
    assertThat(response.potentialAbv().unit()).isEqualTo("% ABV");
    assertThat(response.method()).isEqualTo(AbvPredictionMethod.STANDARD_GRAVITY_DROP);
    assertThat(response.confidence()).isEqualTo(PredictionConfidence.LOW);
  }
}
