package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockProfileEvidenceResponse;
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
        description = """
            Latest contributing MeasurementSet retained as a compatibility anchor. It is not the
            source of every characteristic in the composite current profile.
            """,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockMeasurementResponse latestMeasurement,
    @Schema(
        description = "Compact composite profile summary assembled independently per characteristic.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockBatchDetailProfileSummaryResponse profileSummary,
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
    @Nullable FeedstockBatchDetailSugarAcidityResponse sugarAcidityAssessment,
    @Schema(
        description = "Potential ABV prediction from the SG evidence selected for the current profile.",
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
        description = "Worst freshness status among the planning-required SG and TA evidence.",
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
