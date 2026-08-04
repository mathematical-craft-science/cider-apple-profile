package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
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


@Schema(description = "Feedstock Workspace batch read model for one feedstock batch.")
public record FeedstockWorkspaceBatchResponse(
    @Schema(description = "Feedstock batch metadata and available volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchResponse batch,
    @Schema(
        description = "Current immutable state of the batch material.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(
        description = """
            Metadata of the latest MeasurementSet contributing evidence. The current profile is
            composite and may use older evidence for other characteristics.
            """,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceCurrentMeasurementResponse currentMeasurement,
    @Schema(
        description = """
            Compact composite current profile projection for board rows. Characteristics may come
            from different MeasurementSets; profileEvidence identifies every selected source.
            """,
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
        description = "Worst freshness status among the planning-required SG and TA evidence.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileFreshnessResponse measurementFreshness,
    @Schema(
        description = "Sugar-acidity graph projection of the current SG and TA evidence.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfileGraphResponse sugarAcidity,
    @Schema(
        description = "Potential ABV prediction from current Specific Gravity evidence.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockCurrentProfilePotentialAbvResponse potentialAbv,
    @Schema(
        description = "Readiness assessment for using the batch in composition planning.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockPlanningReadinessResponse planningReadiness,
    @Schema(
        description = "Data quality summary for the current batch evidence.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockDataQualityResponse dataQuality,
    @Schema(
        description = "Fermentation-start readiness assessment for current profile.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FermentationStartReadinessAssessmentResponse fermentationStartReadiness,
    @Schema(
        description = "Target-aware candidate data when request contains target and batch is scorable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceTargetCandidateResponse targetCandidate
) {
}
