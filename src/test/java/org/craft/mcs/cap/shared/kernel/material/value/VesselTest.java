package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class VesselTest {

  @Test
  void normalizesValidVesselName() {
    assertThat(Vessel.named("  Tank F-03  "))
        .extracting(Vessel::name)
        .isEqualTo("Tank F-03");
  }

  @Test
  void rejectsBlankVesselName() {
    assertThatThrownBy(() -> Vessel.named("   "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("vessel name cannot be blank");
  }

  @Test
  void rejectsVesselNameLongerThanDomainLimit() {
    final var name = "a".repeat(161);

    assertThatThrownBy(() -> Vessel.named(name))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("vessel name length must not exceed 160 characters");
  }
}
