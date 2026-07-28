package org.craft.mcs.cap.shared.kernel.chemical.axis;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AxisRangeTest {

  @Test
  void rejectsEmptyAndReversedRanges() {
    assertThatThrownBy(() -> AxisRange.between("2", "2"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("axis range minimum must be less than its maximum");

    assertThatThrownBy(() -> AxisRange.between("2", "1"))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("axis range minimum must be less than its maximum");
  }
}
