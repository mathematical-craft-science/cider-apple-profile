package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityZoneAssessmentResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Current batch sugar-acidity graph projection and capability assessment.")
public record FeedstockWorkspaceSugarAcidityResponse(
    @Schema(description = "Whether current profile can be plotted on the sugar-acidity graph.", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean plottable,
    @Schema(description = "Sugar-acidity zone assessment when plottable.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityZoneAssessmentResponse assessment,
    @Schema(description = "Capability assessment for sugar-acidity plotting.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
