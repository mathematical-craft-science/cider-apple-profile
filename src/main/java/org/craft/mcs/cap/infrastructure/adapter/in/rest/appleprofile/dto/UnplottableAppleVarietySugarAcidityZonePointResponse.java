package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileCapabilityAssessmentResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Apple variety that cannot be plotted because required chemical profile measurements are missing.")
public record UnplottableAppleVarietySugarAcidityZonePointResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", example = "6d62d888-2a2b-4f47-8f21-2ab2d54d8c2f") @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.") @NonNull String varietyName,
    @Schema(description = "Capability assessment explaining missing measurements.") @NonNull ChemicalProfileCapabilityAssessmentResponse capabilityAssessment
) {
}
