package org.craft.mcs.cap.appleprofile.analysis.application.classification.contract;

import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanClassification;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class AppleVarietyNorthAmericanClassificationTest {

  @Test
  void acceptsCompleteProfile() {
    assertThatCode(() -> classification(completeProfile()))
        .doesNotThrowAnyException();
  }

  @Test
  void rejectsPartialProfile() {
    final var partialProfile = new AppleVarietyNorthAmericanAssessmentView(
        AppleVarietyNorthAmericanAssessmentView.SugarLevel.MEDIUM,
        AppleVarietyNorthAmericanAssessmentView.AcidityLevel.HIGH,
        null
    );

    assertThatThrownBy(() -> classification(partialProfile))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("North American classification requires a complete profile");
  }

  private static @NonNull AppleVarietyNorthAmericanClassification classification(
      @NonNull AppleVarietyNorthAmericanAssessmentView profile
  ) {
    return new AppleVarietyNorthAmericanClassification(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Dabinett"),
        profile
    );
  }

  private static @NonNull AppleVarietyNorthAmericanAssessmentView completeProfile() {
    return new AppleVarietyNorthAmericanAssessmentView(
        AppleVarietyNorthAmericanAssessmentView.SugarLevel.MEDIUM,
        AppleVarietyNorthAmericanAssessmentView.AcidityLevel.LOW,
        AppleVarietyNorthAmericanAssessmentView.TanninLevel.HIGH
    );
  }
}
