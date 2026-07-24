package org.craft.mcs.cap.core.feedstock.application.detail.model;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.application.measurement.model.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileEvidenceView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentMeasurementFreshnessView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfilePotentialAbvView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Current feedstock profile read model for the detail page.
 *
 * <p>The model is assembled independently of current evidence for each characteristic. It is not
 * an edit model and does not change feedstock aggregate state.
 */
public record FeedstockBatchCurrentProfileView(
    @Nullable FeedstockMeasurementSummary latestMeasurement,
    @NonNull List<@NonNull FeedstockCurrentProfileEvidenceView> profileEvidence,
    @Nullable FeedstockCurrentMeasurementFreshnessView measurementFreshness,
    @Nullable SugarAcidityProfileEvaluation sugarAcidity,
    @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv,
    @Nullable AxisValue yeastAssimilableNitrogen,
    @NonNull FeedstockPlanningReadinessView planningReadiness,
    @NonNull FeedstockCurrentProfileDataQualityView dataQuality,
    @Nullable FermentationStartReadinessAssessment fermentationStartReadiness,
    @Nullable FeedstockBatchCurrentProfileOperationView lastOperation
) {

  public FeedstockBatchCurrentProfileView {
    profileEvidence = List.copyOf(requireNonNull(profileEvidence, "feedstock profile evidence cannot be null"));
    requireNonNull(planningReadiness, "feedstock detail planning readiness cannot be null");
    requireNonNull(dataQuality, "feedstock detail data quality cannot be null");
  }

  public FeedstockBatchCurrentProfileView(
      @Nullable FeedstockMeasurementSummary latestMeasurement,
      @Nullable FeedstockCurrentMeasurementFreshnessView measurementFreshness,
      @Nullable SugarAcidityProfileEvaluation sugarAcidity,
      @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv,
      @Nullable AxisValue yeastAssimilableNitrogen,
      @NonNull FeedstockPlanningReadinessView planningReadiness,
      @NonNull FeedstockCurrentProfileDataQualityView dataQuality,
      @Nullable FermentationStartReadinessAssessment fermentationStartReadiness,
      @Nullable FeedstockBatchCurrentProfileOperationView lastOperation
  ) {
    this(
        latestMeasurement,
        List.of(),
        measurementFreshness,
        sugarAcidity,
        potentialAbv,
        yeastAssimilableNitrogen,
        planningReadiness,
        dataQuality,
        fermentationStartReadiness,
        lastOperation
    );
  }
}
