package org.craft.mcs.cap.core.cideranalysis.readiness.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ReadinessRuleSourceTest {

  @Test
  void keepsPhAndYanSourcesExplicitlyDistinct() {
    assertThat(ReadinessRuleSource.curatedPhV1().code()).contains("PH");
    assertThat(ReadinessRuleSource.curatedYanV1().code()).contains("YAN");
  }

  @Test
  void rejectsBlankFields() {
    assertThatThrownBy(() -> new ReadinessRuleSource(" ", "source"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("readiness rule source fields cannot be blank");
    assertThatThrownBy(() -> new ReadinessRuleSource("CURATED", " "))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("readiness rule source fields cannot be blank");
  }
}
