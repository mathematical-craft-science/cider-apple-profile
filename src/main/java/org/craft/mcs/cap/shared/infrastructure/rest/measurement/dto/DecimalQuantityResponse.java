package org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


@Schema(description = "Decimal physical quantity in CAP canonical transport units.")
public record DecimalQuantityResponse(
    @Schema(description = "Numeric value.", example = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal value,
    @Schema(description = "Canonical unit label.", example = "L", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String unit
) {
}
