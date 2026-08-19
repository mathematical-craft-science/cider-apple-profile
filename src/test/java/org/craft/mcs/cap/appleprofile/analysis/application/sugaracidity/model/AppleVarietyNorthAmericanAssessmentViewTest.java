package org.craft.mcs.cap.appleprofile.analysis.application.sugaracidity.model;

import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanSugarLevel;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleVarietyNorthAmericanAssessmentViewTest {

  @Test
  void allowsPartialAssessment() {
    final var assessment = new AppleVarietyNorthAmericanAssessmentView(
        NorthAmericanSugarLevel.HIGH,
        null,
        null
    );

    assertThat(assessment)
        .returns(NorthAmericanSugarLevel.HIGH, AppleVarietyNorthAmericanAssessmentView::sugar)
        .returns(null, AppleVarietyNorthAmericanAssessmentView::acidity)
        .returns(null, AppleVarietyNorthAmericanAssessmentView::tannin);
  }

  @Test
  void rejectsEmptyAssessment() {
    assertThatThrownBy(() -> new AppleVarietyNorthAmericanAssessmentView(null, null, null))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("North American assessment cannot be empty");
  }
}
