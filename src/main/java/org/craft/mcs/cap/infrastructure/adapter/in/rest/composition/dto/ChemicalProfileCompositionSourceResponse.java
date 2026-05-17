package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Composition source contribution response.")
public record ChemicalProfileCompositionSourceResponse(
    @Schema(description = "Source reference.")
    @NonNull ChemicalProfileCompositionSourceReferenceRequest reference,
    @Schema(description = "Requested source volume in liters, when absolute volume was used.", example = "25.0")
    @Nullable BigDecimal requestedVolumeLiters,
    @Schema(description = "Relative source proportion, when proportion was used or calculated.", example = "0.25")
    @Nullable BigDecimal proportion
) {
}
