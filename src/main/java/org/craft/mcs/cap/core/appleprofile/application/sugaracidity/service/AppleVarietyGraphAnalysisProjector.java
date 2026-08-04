package org.craft.mcs.cap.core.appleprofile.application.sugaracidity.service;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.application.sugaracidity.model.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyId;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyReferenceProfile;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPrediction;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Projects an apple reference profile into the independent analyses required by graph exploration.
 */
@Component
@RequiredArgsConstructor
public final class AppleVarietyGraphAnalysisProjector {

  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase graphEvaluator;
  private final @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  public @NonNull AppleVarietyGraphAnalysisView project(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName varietyName,
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    requireNonNull(varietyId, "apple variety id cannot be null");
    requireNonNull(varietyName, "apple variety name cannot be null");
    requireNonNull(referenceProfile, "apple variety reference profile cannot be null");

    final var graphEvaluation = graphEvaluator.evaluate(
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity()
    );

    return new AppleVarietyGraphAnalysisView(
        varietyId,
        varietyName,
        referenceProfile.specificGravity(),
        referenceProfile.titratableAcidity(),
        referenceProfile.tannin(),
        graphEvaluation,
        northAmericanAssessment(referenceProfile),
        potentialAbv(referenceProfile)
    );
  }

  private @Nullable AppleVarietyNorthAmericanAssessmentView northAmericanAssessment(
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    final var specificGravity = referenceProfile.specificGravity();
    final var titratableAcidity = referenceProfile.titratableAcidity();
    final var tannin = referenceProfile.tannin();
    if (specificGravity == null && titratableAcidity == null && tannin == null) {
      return null;
    }
    return new AppleVarietyNorthAmericanAssessmentView(
        specificGravity == null
            ? null
            : northAmericanProfileCalculator.assessSugar(requireNonNull(specificGravity)),
        titratableAcidity == null
            ? null
            : northAmericanProfileCalculator.assessAcidity(requireNonNull(titratableAcidity)),
        tannin == null
            ? null
            : northAmericanProfileCalculator.assessTannin(requireNonNull(tannin))
    );
  }

  private @Nullable PotentialAbvPrediction potentialAbv(
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    final var specificGravity = referenceProfile.specificGravity();
    return specificGravity == null
        ? null
        : potentialAbvPredictor.predict(
            SpecificGravityValue.of(midpoint(specificGravity))
        );
  }
}
