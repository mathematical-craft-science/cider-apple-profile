package org.craft.mcs.cap.core.profileanalysis.abv.application.compatibility;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.FixedFinalGravityPredictionStrategy;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.StandardGravityDropAbvPredictionStrategy;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileAxesPotentialAbvAdapterTest {

  private final PotentialAbvPredictor predictor = new PotentialAbvPredictor(
      FixedFinalGravityPredictionStrategy.dryCiderDefault(),
      new StandardGravityDropAbvPredictionStrategy()
  );

  @Test
  void predictsWhenSpecificGravityIsPresent() {
    final var prediction = ChemicalProfileAxesPotentialAbvAdapter.predict(
        ChemicalProfileAxes.of(
            new AxisValue(
                ChemicalAxis.SPECIFIC_GRAVITY,
                new BigDecimal("1.052"),
                AxisValueKind.POINT,
                null,
                null
            ),
            null,
            null
        ),
        predictor
    );

    assertThat(prediction)
        .get()
        .satisfies(value -> assertThat(value.potentialAbv().percent()).isEqualByComparingTo("6.825"));
  }

  @Test
  void returnsEmptyWhenSpecificGravityIsMissing() {
    assertThat(ChemicalProfileAxesPotentialAbvAdapter.predict(
        ChemicalProfileAxes.of(null, null, null),
        predictor
    )).isEmpty();
  }
}
