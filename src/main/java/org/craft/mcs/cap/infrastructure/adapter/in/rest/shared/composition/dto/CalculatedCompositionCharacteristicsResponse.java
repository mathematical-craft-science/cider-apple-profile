package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Canonical characteristics calculated for a composition.
 */
@Schema(description = "Canonical characteristics calculated for a composition.")
public record CalculatedCompositionCharacteristicsResponse(
    @Schema(
        description = "Calculated Specific Gravity (SG).",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse specificGravity,
    @Schema(
        description = "Calculated Titratable Acidity (TA) as malic acid.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse titratableAcidity,
    @Schema(
        description = "Calculated tannin as TAE when all required source values are available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse tannin,
    @Schema(
        description = "Calculated yeast-assimilable nitrogen when all source values are available.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse yeastAssimilableNitrogen
) {
}
