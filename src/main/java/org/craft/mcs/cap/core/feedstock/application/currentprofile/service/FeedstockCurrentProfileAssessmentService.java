package org.craft.mcs.cap.core.feedstock.application.currentprofile.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanProfileCalculator;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessor;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartProfile;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
import org.craft.mcs.cap.core.profileanalysis.graph.application.port.in.EvaluateJolicoeurSugarAcidityGraphUseCase;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/** Composes the reusable current-profile assessments shared by Feedstock application read models. */
@Component
@RequiredArgsConstructor
public final class FeedstockCurrentProfileAssessmentService {

  private final @NonNull EvaluateJolicoeurSugarAcidityGraphUseCase graphEvaluator;
  private final @NonNull NorthAmericanProfileCalculator northAmericanProfileCalculator;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;
  private final @NonNull FermentationStartReadinessAssessor fermentationStartReadinessAssessor;
  private final @NonNull FeedstockCurrentProfileFreshnessAssessor freshnessAssessor;
  private final @NonNull FeedstockPlanningReadinessAssessor planningReadinessAssessor;
  private final @NonNull FeedstockProfileDataQualityAssessor dataQualityAssessor;

  public @NonNull FeedstockCurrentProfileAssessmentView assess(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockCurrentProfileView currentProfile
  ) {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    if (currentProfile == null) {
      return new FeedstockCurrentProfileAssessmentView(
          List.of(),
          null,
          null,
          null,
          null,
          planningReadinessAssessor.assessWithoutCurrentProfile(batch),
          dataQualityAssessor.assessWithoutCurrentProfile(),
          null
      );
    }

    final var graphEvaluation = graphEvaluator.evaluate(
        currentProfile.specificGravity(),
        currentProfile.titratableAcidity()
    );
    final var freshness = freshnessAssessor.assess(currentProfile);
    return new FeedstockCurrentProfileAssessmentView(
        currentProfile.evidence(),
        freshness,
        graphEvaluation,
        northAmericanTanninLevel(currentProfile, graphEvaluation),
        potentialAbv(currentProfile.specificGravity()),
        planningReadinessAssessor.assess(batch, currentProfile),
        dataQualityAssessor.assess(
            currentProfile,
            graphEvaluation,
            freshness,
            currentProfile.evidence()
        ),
        fermentationStartReadinessAssessor.assess(
            new FermentationStartProfile(
                currentProfile.specificGravity(),
                currentProfile.ph(),
                currentProfile.yeastAssimilableNitrogen()
            )
        )
    );
  }

  private @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv(
      @Nullable CharacteristicValue<SpecificGravityValue> specificGravity
  ) {
    if (specificGravity == null) {
      return null;
    }
    return new FeedstockCurrentProfilePotentialAbvView(
        specificGravity,
        potentialAbvPredictor.predict(SpecificGravityValue.of(midpoint(specificGravity)))
    );
  }

  private @Nullable NorthAmericanTanninLevel northAmericanTanninLevel(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull JolicoeurSugarAcidityGraphEvaluation graphEvaluation
  ) {
    if (!graphEvaluation.plottable() || currentProfile.tannin() == null) {
      return null;
    }
    return northAmericanProfileCalculator.assessTannin(currentProfile.tannin()); // NOSONAR
  }

}
