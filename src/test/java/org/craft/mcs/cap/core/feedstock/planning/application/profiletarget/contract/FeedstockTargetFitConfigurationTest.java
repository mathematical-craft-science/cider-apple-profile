package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract;

import java.util.stream.Stream;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.Normalizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


class FeedstockTargetFitConfigurationTest {

  @ParameterizedTest
  @MethodSource("scenarioNormalizers")
  void resolvesScenarioNormalizer(
      AnalysisScenario scenario,
      Normalizer expectedNormalizer
  ) {
    final var configuration = new FeedstockTargetFitConfiguration(
        scenario,
        FeedstockTargetFitConfiguration.ObjectiveWeights.balanced()
    );

    assertThat(configuration.normalizer()).isEqualTo(expectedNormalizer);
  }

  private static Stream<Arguments> scenarioNormalizers() {
    return Stream.of(
        Arguments.of(AnalysisScenario.GRAPH_EXPLORATION, Normalizer.MIN_MAX),
        Arguments.of(AnalysisScenario.BLENDING_PLANNING, Normalizer.EFFECTIVE_RANGE),
        Arguments.of(AnalysisScenario.TARGET_ZONE_MATCHING, Normalizer.THRESHOLD_SEGMENT)
    );
  }
}
