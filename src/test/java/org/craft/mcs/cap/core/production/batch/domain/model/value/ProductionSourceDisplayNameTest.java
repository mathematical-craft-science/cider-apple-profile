package org.craft.mcs.cap.core.production.batch.domain.model.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ProductionSourceDisplayNameTest {

  @Test
  void normalizesSurroundingWhitespace() {
    assertThat(ProductionSourceDisplayName.of("  Late juice  ").value()).isEqualTo("Late juice");
  }

  @Test
  void rejectsBlankName() {
    assertThatThrownBy(() -> ProductionSourceDisplayName.of("  "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source display name cannot be blank");
  }

  @Test
  void rejectsNameLongerThanMaximum() {
    assertThatThrownBy(() -> ProductionSourceDisplayName.of("x".repeat(256)))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("production source display name length must not exceed 255 characters");
  }
}
