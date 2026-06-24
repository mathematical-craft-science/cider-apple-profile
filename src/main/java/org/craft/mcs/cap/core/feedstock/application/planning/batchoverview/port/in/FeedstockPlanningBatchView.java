package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Batch row in the feedstock planning read model.
 *
 * <p>The view keeps the batch as the primary entity and attaches latest-measurement-derived
 * analysis, planning diagnostics and optional target assessment without changing feedstock
 * aggregate state.
 */
public record FeedstockPlanningBatchView(
    @NonNull FeedstockBatchSummary batch,
    @Nullable FeedstockPlanningCurrentMeasurementView currentMeasurement,
    @Nullable FeedstockPlanningMeasurementFreshnessView measurementFreshness,
    @Nullable SugarAcidityProfileEvaluation sugarAcidity,
    @Nullable FeedstockPlanningPotentialAbvView potentialAbv,
    @Nullable AxisValue yeastAssimilableNitrogen,
    @NonNull FeedstockPlanningReadinessView planningReadiness,
    @NonNull FeedstockPlanningDataQualityView dataQuality,
    @Nullable FermentationStartReadinessAssessment fermentationStartReadiness,
    @Nullable FeedstockPlanningTargetCandidateView targetCandidate
) {

  public FeedstockPlanningBatchView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(planningReadiness, "feedstock planning planning readiness cannot be null");
    requireNonNull(dataQuality, "feedstock planning data quality cannot be null");
  }
}
