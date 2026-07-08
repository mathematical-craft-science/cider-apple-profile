package org.craft.mcs.cap.core.profileanalysis.ph.domain;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.measurement.ExactMeasurement;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class PhPlanningGuidancePolicyTest {

  private final PhPlanningGuidancePolicy policy = PhPlanningGuidancePolicy.curatedCiderV1();

  @Test
  void returnsMissingGuidanceWhenPhIsAbsent() {
    final var guidance = policy.assess(ChemicalProfile.builder()
        .sugar(ExactMeasurement.of(SugarConcentration.ofSpecificGravity(SpecificGravityValue.of("1.050"))))
        .build());

    assertThat(guidance)
        .returns(PhPlanningGuidanceCode.PH_MISSING, PhPlanningGuidance::code)
        .returns(null, PhPlanningGuidance::value);
  }

  @Test
  void returnsLowGuidanceWhenPhIsBelowPlanningRange() {
    final var guidance = policy.assess(profile("3.10"));

    assertThat(guidance)
        .returns(PhPlanningGuidanceCode.PH_PLANNING_LOW, PhPlanningGuidance::code)
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("3.10"));
  }

  @Test
  void returnsIdealGuidanceWhenPhIsInsidePlanningRange() {
    final var guidance = policy.assess(profile("3.45"));

    assertThat(guidance)
        .returns(PhPlanningGuidanceCode.PH_PLANNING_IDEAL, PhPlanningGuidance::code)
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("3.45"));
  }

  @Test
  void returnsHighGuidanceWhenPhIsAbovePlanningRange() {
    final var guidance = policy.assess(profile("3.95"));

    assertThat(guidance)
        .returns(PhPlanningGuidanceCode.PH_PLANNING_HIGH, PhPlanningGuidance::code)
        .satisfies(result -> assertThat(result.value()).isEqualByComparingTo("3.95"));
  }

  private static ChemicalProfile profile(String ph) {
    return ChemicalProfile.builder()
        .ph(ExactMeasurement.of(PhValue.of(ph)))
        .build();
  }
}
