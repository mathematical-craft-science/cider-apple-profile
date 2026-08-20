package org.craft.mcs.cap.core.cideranalysis.readiness.application.contract;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class FermentationStartReadinessViewTest {

  @Test
  void rejectsBlankMethod() {
    assertThatThrownBy(() -> new FermentationStartReadinessView(
        FermentationStartReadinessView.Status.READY,
        " ",
        List.of()
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("fermentation-start readiness method cannot be blank");
  }

  @Test
  void rejectsBlankCheckMessage() {
    assertThatThrownBy(() -> new FermentationStartReadinessView.Check(
        FermentationStartReadinessView.Factor.SPECIFIC_GRAVITY,
        FermentationStartReadinessView.Severity.INFO,
        FermentationStartReadinessView.Code.SPECIFIC_GRAVITY_PRESENT,
        " ",
        null,
        null,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("readiness message cannot be blank");
  }

  @Test
  void rejectsCheckValueWithoutUnit() {
    assertThatThrownBy(() -> new FermentationStartReadinessView.Check(
        FermentationStartReadinessView.Factor.PH,
        FermentationStartReadinessView.Severity.INFO,
        FermentationStartReadinessView.Code.PH_ACCEPTABLE,
        "Acceptable pH",
        new BigDecimal("3.5"),
        null,
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("readiness value requires a unit");
  }
}
