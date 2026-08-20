package org.craft.mcs.cap.core.feedstock.batch.application.assessment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.port.in.AssessFeedstockCurrentProfileUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.service.FeedstockCurrentProfileFreshnessAssessor;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.abv.port.out.FeedstockPotentialAbvAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.contract.FeedstockNorthAmericanTanninLevel;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.port.out.FeedstockTanninLevelAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.port.out.FeedstockFermentationStartReadinessAnalyzer;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.port.out.FeedstockSugarAcidityGraphAnalyzer;
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
public final class FeedstockCurrentProfileAssessmentService implements AssessFeedstockCurrentProfileUseCase {

  private final @NonNull FeedstockSugarAcidityGraphAnalyzer graphAnalyzer;
  private final @NonNull FeedstockTanninLevelAnalyzer tanninLevelAnalyzer;
  private final @NonNull FeedstockPotentialAbvAnalyzer potentialAbvAnalyzer;
  private final @NonNull FeedstockFermentationStartReadinessAnalyzer fermentationStartReadinessAnalyzer;
  private final @NonNull FeedstockCurrentProfileFreshnessAssessor freshnessAssessor;
  private final @NonNull FeedstockPlanningReadinessAssessor planningReadinessAssessor;
  private final @NonNull FeedstockProfileDataQualityAssessor dataQualityAssessor;

  @Override
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

    final var graphEvaluation = graphAnalyzer.evaluate(
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
        fermentationStartReadinessAnalyzer.assess(
            currentProfile.specificGravity(),
            currentProfile.ph(),
            currentProfile.yeastAssimilableNitrogen()
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
        potentialAbvAnalyzer.predict(SpecificGravityValue.of(midpoint(specificGravity)))
    );
  }

  private @Nullable FeedstockNorthAmericanTanninLevel northAmericanTanninLevel(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull FeedstockSugarAcidityGraphView graphEvaluation
  ) {
    final var tannin = currentProfile.tannin();
    if (!graphEvaluation.plottable() || tannin == null) {
      return null;
    }
    return tanninLevelAnalyzer.assess(tannin); // NOSONAR
  }

}
