package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.optimization.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.CompositionSourceCharacteristicsRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.composition.common.dto.ChemicalProfileCompositionSourceReferenceRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Optimization source with available volume constraint.")
public record ChemicalProfileCompositionOptimizationSourceRequest(
    @Schema(description = "Source reference.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ChemicalProfileCompositionSourceReferenceRequest reference,
    @Schema(description = "Source canonical characteristics.", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("axes")
    @NonNull CompositionSourceCharacteristicsRequest characteristics,
    @Schema(
        description = "Available source volume in liters.",
        example = "120.0",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal availableVolumeLiters
) {
}
