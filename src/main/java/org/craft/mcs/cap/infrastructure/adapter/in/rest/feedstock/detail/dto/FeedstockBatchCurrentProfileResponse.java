package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.measurement.dto.FeedstockMeasurementResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.readiness.dto.FermentationStartReadinessAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityAxisPointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Transport projection of the current chemical profile for feedstock batch details.
 *
 * <p>The response combines latest-measurement evidence, graph assessment, readiness and freshness
 * signals. It is not an editable measurement command model.
 */
@Schema(description = "Current profile read model for a feedstock batch detail page.")
public record FeedstockBatchCurrentProfileResponse(
    @Schema(
        description = "Latest measurement used as the current profile source.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockMeasurementResponse latestMeasurement,
    @Schema(
        description = "Compact profile summary derived from the latest measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailProfileSummaryResponse profileSummary,
    @Schema(
        description = "Sugar-acidity graph assessment for the latest measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailSugarAcidityResponse sugarAcidityAssessment,
    @Schema(
        description = "Potential ABV prediction from latest sugar measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailPotentialAbvResponse potentialAbv,
    @Schema(
        description = "Current YAN axis value in mg N/L when measured.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityAxisPointResponse yeastAssimilableNitrogen,
    @Schema(
        description = "Freshness assessment for latest measurement.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailMeasurementFreshnessResponse measurementFreshness,
    @Schema(description = "Planning readiness for the current profile.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchDetailPlanningReadinessResponse planningReadiness,
    @Schema(description = "Data quality summary for the current profile.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FeedstockBatchDetailDataQualityResponse dataQuality,
    @Schema(
        description = "Fermentation-start readiness assessment for the current profile.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FermentationStartReadinessAssessmentResponse fermentationStartReadiness,
    @Schema(
        description = "Last operation reflected by the current profile.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailLastOperationResponse lastOperation
) {
}
