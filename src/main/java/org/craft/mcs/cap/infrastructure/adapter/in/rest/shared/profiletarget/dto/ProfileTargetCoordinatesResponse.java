package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "SG/TA coordinate in canonical graph units.")
public record ProfileTargetCoordinatesResponse(
    @Schema(description = "Specific Gravity (SG) coordinate.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse specificGravity,
    @Schema(description = "Titratable Acidity (TA) coordinate.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse titratableAcidity
) {
}
