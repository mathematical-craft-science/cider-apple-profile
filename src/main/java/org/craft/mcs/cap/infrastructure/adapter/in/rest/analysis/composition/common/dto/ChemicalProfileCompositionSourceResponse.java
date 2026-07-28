package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Composition source contribution response.")
public record ChemicalProfileCompositionSourceResponse(
    @Schema(description = "Source reference.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCompositionSourceReferenceRequest reference,
    @Schema(
        description = "Requested source volume, when absolute volume was used.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse requestedVolume,
    @Schema(
        description = "Relative source proportion, when proportion was used or calculated.",
        example = "0.25",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal proportion
) {
}
