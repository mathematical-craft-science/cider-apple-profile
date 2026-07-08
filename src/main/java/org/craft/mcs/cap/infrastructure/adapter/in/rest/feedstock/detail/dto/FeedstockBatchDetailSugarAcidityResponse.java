package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Transport sugar-acidity assessment for the current feedstock profile.
 */
@Schema(description = "Sugar-acidity graph assessment for the feedstock detail current profile.")
public record FeedstockBatchDetailSugarAcidityResponse(
    @Schema(
        description = "Whether the current profile can be plotted on the sugar-acidity graph.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    boolean plottable,
    @Schema(
        description = "Computed graph point coordinates when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityProfilePointResponse point,
    @Schema(
        description = "Sugar-acidity zone assessment when plottable.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Schema(
        description = "Capability assessment for sugar-acidity plotting.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
