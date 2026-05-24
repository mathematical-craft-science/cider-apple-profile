package org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


@Schema(description = "Sugar-acidity coordinate in canonical graph units.")
public record SugarAcidityCoordinatesResponse(
    @Schema(description = "Sugar coordinate in SG.", example = "1.060", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal sugar,
    @Schema(description = "Titratable acidity coordinate in g/L as malic acid.", example = "6.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal acidity
) {
}
