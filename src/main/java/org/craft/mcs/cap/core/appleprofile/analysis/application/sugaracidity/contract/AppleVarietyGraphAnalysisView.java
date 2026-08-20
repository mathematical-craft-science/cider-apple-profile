package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.contract;

import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract.AppleSugarAcidityGraphView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.catalog.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisName;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.contract.ApplePotentialAbvPredictionView;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Catalog analysis view for one apple variety shown in graph-oriented exploration.
 *
 * <p>The view combines independent scenario results needed by the catalog graph and its supporting
 * details. It does not make tannin or North American classification part of the SG/TA graph model.</p>
 */
public record AppleVarietyGraphAnalysisView(
    @NonNull AppleVarietyId varietyId,
    @NonNull AppleVarietyAnalysisName varietyName,
    @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
    @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<TanninValue> tannin,
    @NonNull AppleSugarAcidityGraphView graphEvaluation,
    @Nullable AppleVarietyNorthAmericanAssessmentView northAmericanAssessment,
    @Nullable ApplePotentialAbvPredictionView potentialAbv
) {

  public AppleVarietyGraphAnalysisView {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(graphEvaluation, "sugar-acidity graph evaluation cannot be null");
    validateGraphEvidence(specificGravity, titratableAcidity, graphEvaluation);
    validateNorthAmericanEvidence(
        specificGravity,
        titratableAcidity,
        tannin,
        northAmericanAssessment
    );
    if (potentialAbv != null && specificGravity == null) {
      throw new ApplicationValidationException(
          "apple variety potential ABV requires Specific Gravity evidence"
      );
    }
  }

  private static void validateGraphEvidence(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
      @NonNull AppleSugarAcidityGraphView graphEvaluation
  ) {
    if (graphEvaluation.plottable() && (specificGravity == null || titratableAcidity == null)) {
      throw new ApplicationValidationException(
          "plottable apple variety graph analysis requires Specific Gravity and Titratable Acidity"
      );
    }
  }

  private static void validateNorthAmericanEvidence(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity,
      @Nullable CharacteristicValue<TitratableAcidityValue> titratableAcidity,
      @Nullable CharacteristicValue<TanninValue> tannin,
      @Nullable AppleVarietyNorthAmericanAssessmentView northAmericanAssessment
  ) {
    if (northAmericanAssessment == null) {
      return;
    }
    if (northAmericanAssessment.sugar() != null && specificGravity == null) {
      throw new ApplicationValidationException(
          "North American sugar assessment requires Specific Gravity evidence"
      );
    }
    if (northAmericanAssessment.acidity() != null && titratableAcidity == null) {
      throw new ApplicationValidationException(
          "North American acidity assessment requires Titratable Acidity evidence"
      );
    }
    if (northAmericanAssessment.tannin() != null && tannin == null) {
      throw new ApplicationValidationException(
          "North American tannin assessment requires tannin evidence"
      );
    }
  }
}
