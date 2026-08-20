package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.service;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.contract.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.contract.ApplePotentialAbvPredictionView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.abv.port.out.ApplePotentialAbvAnalyzer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.port.out.AppleCiderClassificationAnalyzer;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.contract.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.classification.contract.AppleVarietyNorthAmericanAssessmentView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.graph.port.out.AppleSugarAcidityGraphAnalyzer;
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

  private final @NonNull AppleSugarAcidityGraphAnalyzer graphAnalyzer;
  private final @NonNull AppleCiderClassificationAnalyzer classificationAnalyzer;
  private final @NonNull ApplePotentialAbvAnalyzer potentialAbvAnalyzer;

  public @NonNull AppleVarietyGraphAnalysisView project(
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    requireNonNull(profile, "apple variety analysis profile cannot be null");

    final var graphEvaluation = graphAnalyzer.evaluate(
        profile.specificGravity(),
        profile.titratableAcidity()
    );

    return new AppleVarietyGraphAnalysisView(
        profile.varietyId(),
        profile.name(),
        profile.specificGravity(),
        profile.titratableAcidity(),
        profile.tannin(),
        graphEvaluation,
        northAmericanAssessment(profile),
        potentialAbv(profile)
    );
  }

  private @Nullable AppleVarietyNorthAmericanAssessmentView northAmericanAssessment(
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    final var specificGravity = profile.specificGravity();
    final var titratableAcidity = profile.titratableAcidity();
    final var tannin = profile.tannin();
    if (specificGravity == null && titratableAcidity == null && tannin == null) {
      return null;
    }
    return classificationAnalyzer.assessNorthAmerican(profile);
  }

  private @Nullable ApplePotentialAbvPredictionView potentialAbv(
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    final var specificGravity = profile.specificGravity();
    return specificGravity == null
        ? null
        : potentialAbvAnalyzer.predict(
            SpecificGravityValue.of(midpoint(specificGravity))
        );
  }
}
