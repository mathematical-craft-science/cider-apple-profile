package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.usecase;

import java.util.stream.Stream;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;


class BuiltInProfileTargetAnalysisScenarioResolverTest {

  private final BuiltInProfileTargetAnalysisScenarioResolver resolver =
      new BuiltInProfileTargetAnalysisScenarioResolver();

  @ParameterizedTest
  @MethodSource("scenarioDefaults")
  void resolvesScenarioToExplicitScoringConfiguration(
      ProfileTargetAnalysisScenario scenario,
      ProfileAxisNormalizerCode expectedNormalizer
  ) {
    final var weights = ProfileTargetObjectiveWeights.balanced();

    final var configuration = resolver.resolve(scenario, weights);

    assertThat(configuration)
        .returns(expectedNormalizer, ProfileTargetScoringConfiguration::normalizerCode)
        .returns(
            ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
            ProfileTargetScoringConfiguration::objectiveFunctionCode
        )
        .returns(weights, ProfileTargetScoringConfiguration::weights);
  }

  private static Stream<Arguments> scenarioDefaults() {
    return Stream.of(
        Arguments.of(ProfileTargetAnalysisScenario.GRAPH_EXPLORATION, ProfileAxisNormalizerCode.MIN_MAX),
        Arguments.of(ProfileTargetAnalysisScenario.BLENDING_PLANNING, ProfileAxisNormalizerCode.EFFECTIVE_RANGE),
        Arguments.of(
            ProfileTargetAnalysisScenario.TARGET_ZONE_MATCHING,
            ProfileAxisNormalizerCode.THRESHOLD_SEGMENT
        )
    );
  }
}
