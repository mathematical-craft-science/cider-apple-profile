package org.craft.mcs.cap.appleprofile.application.sugaracidity.model;

import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanAcidityLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanSugarLevel;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.craft.mcs.cap.core.profileanalysis.testing.ProfileAnalysisTestFixtures.graphEvaluator;
import static org.craft.mcs.cap.support.PotentialAbvPredictionFixtures.prediction;


class AppleVarietyGraphAnalysisViewTest {

  @Test
  void rejectsPlottableEvaluationWithoutMatchingGraphEvidence() {
    final var specificGravity = specificGravity("1.060");
    final var titratableAcidity = titratableAcidity("5.0");

    assertThatThrownBy(() -> new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Kingston Black"),
        specificGravity,
        null,
        null,
        graphEvaluator().evaluate(specificGravity, titratableAcidity),
        null,
        prediction(SpecificGravityValue.of("1.060"))
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
        AppleVarietyName.of("Partial Profile"),
        null,
        null,
        null,
        graphEvaluator().evaluate(null, null),
        new AppleVarietyNorthAmericanAssessmentView(
            NorthAmericanSugarLevel.MEDIUM,
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
        AppleVarietyName.of("Partial Profile"),
        null,
        null,
        null,
        graphEvaluator().evaluate(null, null),
        new AppleVarietyNorthAmericanAssessmentView(
            null,
            NorthAmericanAcidityLevel.MEDIUM,
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
        AppleVarietyName.of("Kingston Black"),
        specificGravity,
        titratableAcidity,
        null,
        graphEvaluator().evaluate(specificGravity, titratableAcidity),
        new AppleVarietyNorthAmericanAssessmentView(
            null,
            null,
            NorthAmericanTanninLevel.MEDIUM
        ),
        prediction(SpecificGravityValue.of("1.060"))
    ))
        .isInstanceOf(ApplicationValidationException.class)
        .hasMessage("North American tannin assessment requires tannin evidence");
  }

  @Test
  void allowsIndependentTanninEvidenceWithoutCompleteGraphProfile() {
    final var tannin = PointCharacteristicValue.of(TanninValue.of("2000"));
    final var view = new AppleVarietyGraphAnalysisView(
        AppleVarietyId.newId(),
        AppleVarietyName.of("Partial Profile"),
        null,
        null,
        tannin,
        graphEvaluator().evaluate(null, null),
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
        AppleVarietyName.of("Partial Profile"),
        null,
        null,
        null,
        graphEvaluator().evaluate(null, null),
        null,
        prediction(SpecificGravityValue.of("1.060"))
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
