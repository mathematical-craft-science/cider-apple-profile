package org.craft.mcs.cap.core.formulation.optimization.application.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class OptimizationObjectiveViewTest {

  @Test
  void rejectsNegativeObjectiveScore() {
    assertThatThrownBy(() -> new OptimizationObjectiveView(new BigDecimal("-0.1"), List.of()))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("optimization score cannot be negative");
  }

  @Test
  void rejectsNegativePenalty() {
    assertThatThrownBy(() -> new OptimizationObjectiveView.Penalty(
        OptimizationObjectiveView.Code.LOW_YEAST_ASSIMILABLE_NITROGEN,
        OptimizationObjectiveView.Severity.WARNING,
        new BigDecimal("-0.1"),
        null,
        null,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("optimization penalty cannot be negative");
  }
}
