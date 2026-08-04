package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockProfileEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileFreshnessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileGraphResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfilePotentialAbvResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockCurrentProfileSummaryResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockDataQualityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto.FeedstockPlanningReadinessResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Transport projection of current measured characteristics for feedstock batch details.
 *
 * <p>The response combines independently selected current evidence, graph assessment, readiness and
 * freshness signals. It is not an editable measurement command model.
 */
@Schema(description = "Current profile read model for a feedstock batch detail page.")
public record FeedstockBatchCurrentProfileResponse(
    @Schema(
        description = "Compact composite profile summary assembled independently per characteristic.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileSummaryResponse profileSummary,
    @Schema(
        description = "Per-characteristic evidence selected for the composite current profile.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull List<@NonNull FeedstockProfileEvidenceResponse> profileEvidence,
    @Schema(
        description = "Sugar-acidity graph assessment for the composite current profile.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileGraphResponse sugarAcidityAssessment,
    @Schema(
        description = "Potential ABV prediction from the SG evidence selected for the current profile.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfilePotentialAbvResponse potentialAbv,
    @Schema(
        description = "Worst freshness status among the planning-required SG and TA evidence.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileFreshnessResponse measurementFreshness,
    @Schema(description = "Planning readiness for the current profile.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockPlanningReadinessResponse planningReadiness,
    @Schema(description = "Data quality summary for the current profile.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockDataQualityResponse dataQuality,
    @Schema(
        description = "Fermentation-start readiness assessment for the current profile.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FermentationStartReadinessAssessmentResponse fermentationStartReadiness
) {
}
