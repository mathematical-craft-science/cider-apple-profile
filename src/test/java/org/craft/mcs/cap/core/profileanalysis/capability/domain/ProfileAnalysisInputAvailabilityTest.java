package org.craft.mcs.cap.core.profileanalysis.capability.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileAnalysisInputAvailabilityTest {

  @ParameterizedTest
  @CsvSource({
      "true, true, true, SUGAR_ACIDITY_GRAPH, true",
      "true, false, true, SUGAR_ACIDITY_GRAPH, false",
      "true, true, true, NORTH_AMERICAN_PROFILE, true",
      "true, true, false, NORTH_AMERICAN_PROFILE, false",
      "true, false, false, ABV_PREDICTION, true",
      "false, true, true, ABV_PREDICTION, false"
  })
  void evaluatesScenarioRequirementsFromAvailabilityFacts(
      boolean specificGravity,
      boolean titratableAcidity,
      boolean tannin,
      ProfileAnalysisCapability capability,
      boolean expected
  ) {
    assertThat(new ProfileAnalysisInputAvailability(
        specificGravity,
        titratableAcidity,
        tannin
    ).supports(capability)).isEqualTo(expected);
  }
}
