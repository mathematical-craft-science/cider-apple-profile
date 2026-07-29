package org.craft.mcs.cap.core.profileanalysis.classification.domain.rule;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ClassificationValueRangeTest {

  @Test
  void rejectsEmptyAndReversedRanges() {
    assertThatThrownBy(() -> ClassificationValueRange.between("2", "2"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("classification range minimum must be less than its maximum");

    assertThatThrownBy(() -> ClassificationValueRange.between("2", "1"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("classification range minimum must be less than its maximum");
  }
}
