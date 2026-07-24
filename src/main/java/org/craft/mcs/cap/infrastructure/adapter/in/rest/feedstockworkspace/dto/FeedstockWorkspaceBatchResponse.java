package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockProfileEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock Workspace batch read model for one feedstock batch.")
public record FeedstockWorkspaceBatchResponse(
    @Schema(description = "Feedstock batch metadata and available volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchResponse batch,
    @Schema(
        description = """
            Latest contributing MeasurementSet retained as a compatibility anchor. The current
            profile is composite and may use older evidence for other characteristics.
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
    @Nullable FeedstockWorkspaceProfileSummaryResponse profileSummary,
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
    @Nullable FeedstockWorkspaceMeasurementFreshnessResponse measurementFreshness,
    @Schema(
        description = "Sugar-acidity graph projection of the current measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspaceSugarAcidityResponse sugarAcidity,
    @Schema(
        description = "Potential ABV prediction from current sugar measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockWorkspacePotentialAbvResponse potentialAbv,
    @Schema(
        description = "Current YAN axis value in mg N/L when measured.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityAxisPointResponse yeastAssimilableNitrogen,
    @Schema(
        description = "Readiness assessment for using the batch in composition planning.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockWorkspacePlanningReadinessResponse planningReadiness,
    @Schema(
        description = "Data quality summary for the current batch evidence.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FeedstockWorkspaceDataQualityResponse dataQuality,
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
