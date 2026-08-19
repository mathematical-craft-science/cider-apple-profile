package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.northamerican.NorthAmericanTanninLevel;
import org.craft.mcs.cap.core.profileanalysis.fermentationstart.domain.FermentationStartReadinessAssessment;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityGraphEvaluation;
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
    @Nullable JolicoeurSugarAcidityGraphEvaluation graphEvaluation,
    @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
    @Nullable FeedstockCurrentProfilePotentialAbvView potentialAbv,
    @NonNull FeedstockPlanningReadinessView planningReadiness,
    @NonNull FeedstockCurrentProfileDataQualityView dataQuality,
    @Nullable FermentationStartReadinessAssessment fermentationStartReadiness
) {

  public FeedstockCurrentProfileAssessmentView {
    profileEvidence = List.copyOf(requireNonNull(profileEvidence, "feedstock profile evidence cannot be null"));
    requireNonNull(planningReadiness, "feedstock planning readiness cannot be null");
    requireNonNull(dataQuality, "feedstock current profile data quality cannot be null");
  }
}
