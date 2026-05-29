package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.calculation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionAxesRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Composition source with chemical axes and selected weight.")
public record ChemicalProfileCompositionSourceRequest(
    @Schema(description = "Source reference.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCompositionSourceReferenceRequest reference,
    @Schema(description = "Source canonical chemical axes.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCompositionAxesRequest axes,
    @Schema(description = "Source weight.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCompositionSourceWeightRequest weight,
    @Schema(description = "Available source volume in liters, when volume constraints are known.", example = "120.0", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal availableVolumeLiters
) {
}
