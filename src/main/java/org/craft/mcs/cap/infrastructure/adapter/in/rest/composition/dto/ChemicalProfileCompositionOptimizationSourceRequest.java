package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Optimization source with available volume constraint.")
public record ChemicalProfileCompositionOptimizationSourceRequest(
    @Schema(description = "Source reference.")
    @NonNull ChemicalProfileCompositionSourceReferenceRequest reference,
    @Schema(description = "Source canonical chemical axes.")
    @NonNull ChemicalProfileCompositionAxesRequest axes,
    @Schema(description = "Available source volume in liters.", example = "120.0")
    @Nullable BigDecimal availableVolumeLiters
) {
}
