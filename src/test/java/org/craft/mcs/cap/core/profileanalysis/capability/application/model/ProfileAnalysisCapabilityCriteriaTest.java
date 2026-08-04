package org.craft.mcs.cap.core.profileanalysis.capability.application.model;

import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ProfileAnalysisCapabilityCriteriaTest {

  @Test
  void providesCapableAndNotCapableFactories() {
    assertThat(ProfileAnalysisCapabilityCriteria.capable(
        ProfileAnalysisCapability.SUGAR_ACIDITY_GRAPH
    ).capable())
        .isTrue();
    assertThat(ProfileAnalysisCapabilityCriteria.notCapable(
        ProfileAnalysisCapability.SUGAR_ACIDITY_GRAPH
    ).capable())
        .isFalse();
  }
}
