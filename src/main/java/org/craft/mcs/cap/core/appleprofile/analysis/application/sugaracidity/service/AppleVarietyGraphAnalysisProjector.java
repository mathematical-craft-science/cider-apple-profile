package org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.service;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.appleprofile.analysis.application.profile.model.AppleVarietyAnalysisProfile;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyGraphAnalysisView;
import org.craft.mcs.cap.core.appleprofile.analysis.application.sugaracidity.model.AppleVarietyNorthAmericanAssessmentView;
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
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    requireNonNull(profile, "apple variety analysis profile cannot be null");

    final var graphEvaluation = graphEvaluator.evaluate(
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
      @NonNull AppleVarietyAnalysisProfile profile
  ) {
    final var specificGravity = profile.specificGravity();
    return specificGravity == null
        ? null
        : potentialAbvPredictor.predict(
            SpecificGravityValue.of(midpoint(specificGravity))
        );
  }
}
