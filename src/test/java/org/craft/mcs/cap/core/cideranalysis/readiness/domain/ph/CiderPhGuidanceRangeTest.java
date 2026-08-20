package org.craft.mcs.cap.core.cideranalysis.readiness.domain.ph;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class CiderPhGuidanceRangeTest {

  private final CiderPhGuidanceRange range = CiderPhGuidanceRange.curatedCiderV1();

  @Test
  void classifiesValuesOutsideTheRange() {
    assertThat(List.of(
        range.isBelow(new BigDecimal("3.29")),
        range.isBelow(new BigDecimal("3.30")),
        range.isAbove(new BigDecimal("3.80")),
        range.isAbove(new BigDecimal("3.81"))
    )).containsExactly(true, false, false, true);
  }

  @Test
  void rejectsReversedRange() {
    assertThatThrownBy(() -> new CiderPhGuidanceRange(
        new BigDecimal("3.8"),
        new BigDecimal("3.3")
    ))
        .isInstanceOf(DomainRuleViolationException.class)
        .hasMessage("cider pH guidance minimum must not exceed maximum");
  }

  @Test
  void rejectsNullMinimum() {
    assertThatNullPointerException()
        .isThrownBy(() -> new CiderPhGuidanceRange(null, BigDecimal.ONE))
        .withMessage("cider pH guidance minimum cannot be null");
  }

  @Test
  void rejectsNullMaximum() {
    assertThatNullPointerException()
        .isThrownBy(() -> new CiderPhGuidanceRange(BigDecimal.ONE, null))
        .withMessage("cider pH guidance maximum cannot be null");
  }
}
