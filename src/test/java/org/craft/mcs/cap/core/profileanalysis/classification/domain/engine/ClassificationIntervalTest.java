package org.craft.mcs.cap.core.profileanalysis.classification.domain.engine;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ClassificationIntervalTest {

  @Test
  void retainsOrderedBounds() {
    assertThat(new ClassificationInterval(
        new BigDecimal("5.0"),
        new BigDecimal("7.0")
    ))
        .returns(new BigDecimal("5.0"), ClassificationInterval::lower)
        .returns(new BigDecimal("7.0"), ClassificationInterval::upper);
  }

  @Test
  void rejectsReversedBounds() {
    assertThatThrownBy(() -> new ClassificationInterval(
        new BigDecimal("7.0"),
        new BigDecimal("5.0")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("classification interval lower bound cannot be greater than upper bound");
  }
}
