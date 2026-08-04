package org.craft.mcs.cap.appleprofile.domain.model;

import org.craft.mcs.cap.core.appleprofile.domain.model.ReferenceProfileSource;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ReferenceProfileSourceTest {

  @Test
  void normalizesReferenceProfileSource() {
    assertThat(ReferenceProfileSource.of("  WSU cider apple database  "))
        .returns("WSU cider apple database", ReferenceProfileSource::value);
  }

  @Test
  void rejectsBlankReferenceProfileSource() {
    assertThatThrownBy(() -> ReferenceProfileSource.of("  "))
        .isInstanceOf(DomainRuleViolationException.class);
  }

  @Test
  void rejectsReferenceProfileSourceAboveMaximumLength() {
    assertThatThrownBy(() -> ReferenceProfileSource.of("s".repeat(256)))
        .isInstanceOf(DomainRuleViolationException.class);
  }
}
