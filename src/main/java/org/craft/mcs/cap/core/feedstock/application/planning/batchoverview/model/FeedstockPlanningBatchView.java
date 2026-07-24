package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileEvidenceView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Batch row in the feedstock planning read model.
 *
 * <p>The view keeps the batch as the primary entity and attaches composite current-profile
 * analysis, planning diagnostics and optional target assessment without changing aggregate state.
 */
public record FeedstockPlanningBatchView(
    @NonNull FeedstockBatchSummary batch,
    @Nullable FeedstockCurrentMeasurementView currentMeasurement,
    @NonNull List<@NonNull FeedstockCurrentProfileEvidenceView> profileEvidence,
    @Nullable FeedstockCurrentMeasurementFreshnessView measurementFreshness,
    @Nullable SugarAcidityProfileEvaluation sugarAcidity,
    @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv,
    @Nullable AxisValue yeastAssimilableNitrogen,
    @NonNull FeedstockPlanningReadinessView planningReadiness,
    @NonNull FeedstockCurrentProfileDataQualityView dataQuality,
    @Nullable FermentationStartReadinessAssessment fermentationStartReadiness,
    @Nullable FeedstockPlanningTargetCandidateView targetCandidate
) {

  public FeedstockPlanningBatchView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    profileEvidence = List.copyOf(requireNonNull(profileEvidence, "feedstock profile evidence cannot be null"));
    requireNonNull(planningReadiness, "feedstock planning readiness cannot be null");
    requireNonNull(dataQuality, "feedstock current profile data quality cannot be null");
  }

  public FeedstockPlanningBatchView(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockCurrentMeasurementView currentMeasurement,
      @Nullable FeedstockCurrentMeasurementFreshnessView measurementFreshness,
      @Nullable SugarAcidityProfileEvaluation sugarAcidity,
      @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv,
      @Nullable AxisValue yeastAssimilableNitrogen,
      @NonNull FeedstockPlanningReadinessView planningReadiness,
      @NonNull FeedstockCurrentProfileDataQualityView dataQuality,
      @Nullable FermentationStartReadinessAssessment fermentationStartReadiness,
      @Nullable FeedstockPlanningTargetCandidateView targetCandidate
  ) {
    this(
        batch,
        currentMeasurement,
        List.of(),
        measurementFreshness,
        sugarAcidity,
        potentialAbv,
        yeastAssimilableNitrogen,
        planningReadiness,
        dataQuality,
        fermentationStartReadiness,
        targetCandidate
    );
  }
}
