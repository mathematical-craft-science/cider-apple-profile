package org.craft.mcs.cap.appleprofile.analysis.application.sugaracidity.contract;

import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.contract.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView.AcidityLevel;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView.SugarLevel;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView.TanninLevel;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.support.CiderAnalysisTestFixtures.appleGraphAnalyzer;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.applePrediction;


class AppleVarietyGraphAnalysisViewTest {

  @Test
  void rejectsPlottableEvaluationWithoutMatchingGraphEvidence() {
    final var specificGravity = specificGravity("1.060");
    final var titratableAcidity = titratableAcidity("5.0");

    assertThatThrownBy(() -> new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Kingston Black"),
        specificGravity,
        null,
        null,
        appleGraphAnalyzer().evaluate(specificGravity, titratableAcidity),
        null,
        applePrediction(SpecificGravityValue.of("1.060"))
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage(
            "plottable apple variety graph analysis requires Specific Gravity and Titratable Acidity"
        );
  }

  @Test
  void rejectsNorthAmericanSugarAssessmentWithoutSpecificGravityEvidence() {
    assertThatThrownBy(() -> new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Partial Profile"),
        null,
        null,
        null,
        appleGraphAnalyzer().evaluate(null, null),
        new AppleVarietyNorthAmericanAssessmentView(
            SugarLevel.MEDIUM,
            null,
            null
        ),
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("North American sugar assessment requires Specific Gravity evidence");
  }

  @Test
  void rejectsNorthAmericanAcidityAssessmentWithoutTitratableAcidityEvidence() {
    assertThatThrownBy(() -> new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Partial Profile"),
        null,
        null,
        null,
        appleGraphAnalyzer().evaluate(null, null),
        new AppleVarietyNorthAmericanAssessmentView(
            null,
            AcidityLevel.MEDIUM,
            null
        ),
        null
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("North American acidity assessment requires Titratable Acidity evidence");
  }

  @Test
  void rejectsNorthAmericanTanninAssessmentWithoutTanninEvidence() {
    final var specificGravity = specificGravity("1.060");
    final var titratableAcidity = titratableAcidity("5.0");

    assertThatThrownBy(() -> new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Kingston Black"),
        specificGravity,
        titratableAcidity,
        null,
        appleGraphAnalyzer().evaluate(specificGravity, titratableAcidity),
        new AppleVarietyNorthAmericanAssessmentView(
            null,
            null,
            TanninLevel.MEDIUM
        ),
        applePrediction(SpecificGravityValue.of("1.060"))
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("North American tannin assessment requires tannin evidence");
  }

  @Test
  void allowsIndependentTanninEvidenceWithoutCompleteGraphProfile() {
    final var tannin = PointCharacteristicValue.of(TanninValue.of("2000"));
    final var view = new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Partial Profile"),
        null,
        null,
        tannin,
        appleGraphAnalyzer().evaluate(null, null),
        null,
        null
    );

    assertThat(view)
        .returns(tannin, AppleVarietyGraphAnalysisView::tannin)
        .returns(false, value -> value.graphEvaluation().plottable())
        .returns(null, AppleVarietyGraphAnalysisView::northAmericanAssessment);
  }

  @Test
  void rejectsPotentialAbvWithoutSpecificGravityEvidence() {
    assertThatThrownBy(() -> new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyAnalysisName.of("Partial Profile"),
        null,
        null,
        null,
        appleGraphAnalyzer().evaluate(null, null),
        null,
        applePrediction(SpecificGravityValue.of("1.060"))
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("apple variety potential ABV requires Specific Gravity evidence");
  }

  private static PointCharacteristicValue<SpecificGravityValue> specificGravity(String value) {
    return PointCharacteristicValue.of(SpecificGravityValue.of(value));
  }

  private static PointCharacteristicValue<TitratableAcidityValue> titratableAcidity(String value) {
    return PointCharacteristicValue.of(TitratableAcidityValue.of(value));
  }
}
