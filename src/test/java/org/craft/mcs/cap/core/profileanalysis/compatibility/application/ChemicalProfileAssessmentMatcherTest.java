package org.craft.mcs.cap.core.profileanalysis.compatibility.application;

import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.application.model.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentAdapter;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures
    .chemicalProfileAssessmentMatcher;


class ChemicalProfileAssessmentMatcherTest {

  @Test
  void matchesCapabilityAndJolicoeurZoneTogether() {
    final var assessment = chemicalProfileAssessmentAdapter().project(profile("1.065", "5.8"));
    final var matcher = chemicalProfileAssessmentMatcher();

    assertThat(matcher.matches(
        assessment,
        ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH),
        JolicoeurSugarAcidityZoneCriteria.of(JolicoeurSugarAcidityZone.PERFECT_APPLES)
    )).isTrue();
    assertThat(matcher.matches(
        assessment,
        ChemicalProfileCapabilityCriteria.capable(ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH),
        JolicoeurSugarAcidityZoneCriteria.of(JolicoeurSugarAcidityZone.HIGH_SUGAR_APPLES)
    )).isFalse();
  }

  @Test
  void leavesCapabilityUnrestrictedWhenCriterionHasNoExpectedState() {
    final var assessment = chemicalProfileAssessmentAdapter().project(profile("1.065", null));

    assertThat(chemicalProfileAssessmentMatcher().matches(
        assessment,
        new ChemicalProfileCapabilityCriteria(ChemicalProfileCapability.SUGAR_ACIDITY_GRAPH, null),
        null
    )).isTrue();
  }

  private static ChemicalProfile profile(
      @NonNull String specificGravity,
      @Nullable String titratableAcidity
  ) {
    final var builder = ChemicalProfile.builder()
        .sugar(PointCharacteristicValue.of(SugarConcentration.ofSpecificGravity(
            SpecificGravityValue.of(specificGravity)
        )));
    if (titratableAcidity != null) {
      builder.titratableAcidity(PointCharacteristicValue.of(
          TitratableAcidityValue.of(titratableAcidity)
      ));
    }
    return builder.build();
  }
}
