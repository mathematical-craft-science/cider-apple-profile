package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.batch.dto.FeedstockBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Feedstock Workspace batch read model for one feedstock batch.")
public record FeedstockWorkspaceBatchResponse(
    @Schema(description = "Feedstock batch metadata and available volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchResponse batch,
    @Schema(description = "Latest measurement used as current batch profile.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockWorkspaceCurrentMeasurementResponse currentMeasurement,
    @Schema(description = "Sugar-acidity graph projection of the current measurement.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockWorkspaceSugarAcidityResponse sugarAcidity,
    @Schema(description = "Potential ABV prediction from current sugar measurement.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockWorkspacePotentialAbvResponse potentialAbv,
    @Schema(description = "Current YAN axis value in mg N/L when measured.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityAxisPointResponse yeastAssimilableNitrogen,
    @Schema(description = "Fermentation-start readiness assessment for current profile.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FermentationStartReadinessAssessmentResponse fermentationStartReadiness,
    @Schema(description = "Target-aware candidate data when request contains target and batch is scorable.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockWorkspaceTargetCandidateResponse targetCandidate
) {
}
