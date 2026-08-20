package org.craft.mcs.cap.feedstock.batch.application.analysis.readiness.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract
    .FeedstockFermentationStartReadinessView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedstockFermentationStartReadinessViewTest {

  @Test
  void rejectsCheckValueWithoutUnit() {
    assertThatThrownBy(() -> new FeedstockFermentationStartReadinessView.Check(
        FeedstockFermentationStartReadinessView.Factor.PH,
        FeedstockFermentationStartReadinessView.Severity.INFO,
        FeedstockFermentationStartReadinessView.Code.PH_ACCEPTABLE,
        "Acceptable pH",
        new BigDecimal("3.5"),
        null,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("readiness value requires a unit");
  }
}
