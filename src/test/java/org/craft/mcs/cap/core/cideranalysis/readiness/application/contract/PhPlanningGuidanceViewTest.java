package org.craft.mcs.cap.core.cideranalysis.readiness.application.contract;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;


class PhPlanningGuidanceViewTest {

  @Test
  void rejectsValueForMissingPhCode() {
    assertThatThrownBy(() -> new PhPlanningGuidanceView(
        PhPlanningGuidanceView.Code.PH_MISSING,
        "pH is missing",
        new BigDecimal("3.5"),
        "CURATED_CIDER_V1"
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("pH planning guidance value must match its code");
  }

  @Test
  void rejectsAbsentValueForAssessedPhCode() {
    assertThatThrownBy(() -> new PhPlanningGuidanceView(
        PhPlanningGuidanceView.Code.PH_PLANNING_IDEAL,
        "pH is ideal",
        null,
        "CURATED_CIDER_V1"
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("pH planning guidance value must match its code");
  }
}
