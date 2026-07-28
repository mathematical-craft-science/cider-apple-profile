package org.craft.mcs.cap.core.profileanalysis.profileaxis.application.model;

import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ChemicalProfileCapabilityCriteriaTest {

  @Test
  void providesCapableAndNotCapableFactories() {
    assertThat(ChemicalProfileCapabilityCriteria.capable(
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH
    ).capable())
        .isTrue();
    assertThat(ChemicalProfileCapabilityCriteria.notCapable(
        ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH
    ).capable())
        .isFalse();
  }
}
