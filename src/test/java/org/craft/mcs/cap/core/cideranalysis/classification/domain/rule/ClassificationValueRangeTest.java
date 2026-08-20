package org.craft.mcs.cap.core.cideranalysis.classification.domain.rule;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class ClassificationValueRangeTest {

  @Test
  void usesInclusiveLowerAndExclusiveUpperBoundary() {
    final var range = ClassificationValueRange.between("4.5", "7.5");

    assertThat(List.of(
        range.contains(new BigDecimal("4.499")),
        range.contains(new BigDecimal("4.5")),
        range.contains(new BigDecimal("7.499")),
        range.contains(new BigDecimal("7.5"))
    )).containsExactly(false, true, true, false);
  }

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
