package org.craft.mcs.cap.core.cideranalysis.readiness.domain.planning;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.readiness.domain.ReadinessRuleSource;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.PhValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class PhPlanningGuidancePolicyTest {

  private final PhPlanningGuidancePolicy policy = PhPlanningGuidancePolicy.curatedCiderV1();

  @Test
  void returnsMissingGuidanceWhenPhIsAbsent() {
    final var guidance = policy.assess(null);

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

  @Test
  void rejectsGuidanceValueThatContradictsCode() {
    assertThatThrownBy(() -> new PhPlanningGuidance(
        PhPlanningGuidanceCode.PH_MISSING,
        "pH is missing",
        new BigDecimal("3.5"),
        ReadinessRuleSource.curatedPhV1()
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("pH planning guidance value must match its code");
  }

  private static PointCharacteristicValue<PhValue> profile(String ph) {
    return PointCharacteristicValue.of(PhValue.of(ph));
  }
}
