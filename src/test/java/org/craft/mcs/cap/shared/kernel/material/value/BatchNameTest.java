package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class BatchNameTest {

  @Test
  void normalizesValidName() {
    assertThat(BatchName.of("  Summer blend  "))
        .extracting(BatchName::value)
        .isEqualTo("Summer blend");
  }

  @Test
  void rejectsBlankName() {
    assertThatThrownBy(() -> BatchName.of("   "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch name cannot be blank");
  }

  @Test
  void rejectsNameLongerThanDomainLimit() {
    final var name = "a".repeat(161);

    assertThatThrownBy(() -> BatchName.of(name))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("batch name length must not exceed 160 characters");
  }
}
