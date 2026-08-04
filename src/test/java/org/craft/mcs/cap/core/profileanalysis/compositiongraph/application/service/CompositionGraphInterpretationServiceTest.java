package org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.service;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.port.in.CalculateCompositionResult;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculationRequest;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSource;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.MassBalanceCompositionCalculator;
import org.craft.mcs.cap.core.profileanalysis.compositiongraph.application.model.CalculatedCompositionGraphView;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .compositionSourceCharacteristics;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .northAmericanProfileCalculator;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.predictor;


class CompositionGraphInterpretationServiceTest {

  private final CompositionGraphInterpretationService service =
      new CompositionGraphInterpretationService(northAmericanProfileCalculator());

  @ParameterizedTest
  @CsvSource({
      "1499.999, LOW",
      "1500, MEDIUM",
      "2499.999, MEDIUM",
      "2500, HIGH"
  })
  void interpretsCalculatedTanninForGraphPointColor(
      @NonNull String tannin,
      @NonNull NorthAmericanTanninLevel expectedLevel
  ) {
    assertThat(service.interpret(calculation(tannin)))
        .returns(expectedLevel, CalculatedCompositionGraphView::northAmericanTanninLevel);
  }

  @Test
  void omitsGraphTanninLevelWhenCompositionTanninWasNotCalculated() {
    assertThat(service.interpret(calculation(null)).northAmericanTanninLevel())
        .isNull();
  }

  private static @NonNull CalculateCompositionResult calculation(
      @Nullable String tannin
  ) {
    final var composition = new MassBalanceCompositionCalculator().calculate(
        new CompositionCalculationRequest(
            List.of(source(tannin)),
            CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
        )
    );
    return CalculateCompositionResult.withoutTargetDistance(
        composition,
        predictor().predict(composition.characteristics().specificGravity())
    );
  }

  private static @NonNull CompositionSource source(
      @Nullable String tannin
  ) {
    return new CompositionSource(
        new CompositionSourceReference("FEEDSTOCK_BATCH", "source"),
        compositionSourceCharacteristics("1.060", "5.0", tannin, null),
        new AbsoluteCompositionVolume(VolumeValue.ofLiters("10")),
        null
    );
  }
}
