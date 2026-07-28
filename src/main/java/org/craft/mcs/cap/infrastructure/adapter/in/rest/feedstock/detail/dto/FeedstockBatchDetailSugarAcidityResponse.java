package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto.SugarAcidityGraphAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Transport sugar-acidity assessment backed by current feedstock SG and TA evidence.
 */
@Schema(description = "Sugar-acidity graph assessment for the feedstock detail current profile.")
public record FeedstockBatchDetailSugarAcidityResponse(
    @Schema(
        description = "Whether current SG and TA evidence can be plotted on the sugar-acidity graph.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean plottable,
    @Schema(
        description = "Computed graph point coordinates when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityGraphPointResponse point,
    @Schema(
        description = "Sugar-acidity zone assessment when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityGraphAssessmentResponse assessment,
    @Schema(
        description = "Capability assessment for sugar-acidity plotting.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
