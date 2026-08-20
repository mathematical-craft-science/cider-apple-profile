package org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileDataQualityView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileFreshnessView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockPlanningReadinessView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.classification.contract.FeedstockNorthAmericanTanninLevel;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.readiness.contract.FeedstockFermentationStartReadinessView;
import org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract.FeedstockSugarAcidityGraphView;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Common assessment of the current feedstock profile used by planning and detail read models.
 *
 * <p>This is an application projection, not a Feedstock aggregate or measurement source of truth.</p>
 */
public record FeedstockCurrentProfileAssessmentView(
    @NonNull List<@NonNull MeasurementEvidenceView> profileEvidence,
    @Nullable FeedstockCurrentProfileFreshnessView freshness,
    @Nullable FeedstockSugarAcidityGraphView graphEvaluation,
    @Nullable FeedstockNorthAmericanTanninLevel northAmericanTanninLevel,
    @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv,
    @NonNull FeedstockPlanningReadinessView planningReadiness,
    @NonNull FeedstockCurrentProfileDataQualityView dataQuality,
    @Nullable FeedstockFermentationStartReadinessView fermentationStartReadiness
) {

  public FeedstockCurrentProfileAssessmentView {
    profileEvidence = List.copyOf(requireNonNull(profileEvidence, "feedstock profile evidence cannot be null"));
    requireNonNull(planningReadiness, "feedstock planning readiness cannot be null");
    requireNonNull(dataQuality, "feedstock current profile data quality cannot be null");
  }
}
