package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;


class ProfileTargetScoringConfigurationTest {

  @Test
  void preservesExplicitMathematicalConfiguration() {
    final var weights = ProfileTargetObjectiveWeights.balanced();

    final var configuration = new ProfileTargetScoringConfiguration(
        ProfileAxisNormalizerCode.SENSITIVITY_UNIT,
        ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA,
        weights
    );

    assertThat(configuration)
        .returns(ProfileAxisNormalizerCode.SENSITIVITY_UNIT, ProfileTargetScoringConfiguration::normalizerCode)
        .returns(
            ProfileTargetObjectiveFunctionCode.MANHATTAN_DISTANCE_TO_TARGET_AREA,
            ProfileTargetScoringConfiguration::objectiveFunctionCode
        )
        .returns(weights, ProfileTargetScoringConfiguration::weights);
  }

  @Test
  void rejectsMissingConfigurationParts() {
    final var weights = ProfileTargetObjectiveWeights.balanced();

    assertThatNullPointerException()
        .isThrownBy(() -> new ProfileTargetScoringConfiguration(
            null,
            ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
            weights
        ))
        .withMessage("profile axis normalizer code cannot be null");
    assertThatNullPointerException()
        .isThrownBy(() -> new ProfileTargetScoringConfiguration(
            ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
            null,
            weights
        ))
        .withMessage("profile target objective function code cannot be null");
    assertThatNullPointerException()
        .isThrownBy(() -> new ProfileTargetScoringConfiguration(
            ProfileAxisNormalizerCode.EFFECTIVE_RANGE,
            ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA,
            null
        ))
        .withMessage("profile target objective weights cannot be null");
  }
}
