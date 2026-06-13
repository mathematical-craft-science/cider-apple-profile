package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Sugar-acidity coordinate in canonical graph units.")
public record SugarAcidityCoordinatesResponse(
    @Schema(description = "Sugar coordinate.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse sugar,
    @Schema(description = "Titratable acidity coordinate.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse acidity
) {
}
