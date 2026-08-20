package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.classification.NorthAmericanTanninLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = "Jolicoeur SG/TA zone assessment with optional North American tannin interpretation."
)
public record SugarAcidityGraphAssessmentResponse(
    @Schema(
        description = "Jolicoeur zone code, or null when the point does not belong to a configured zone.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Optional North American tannin level. It does not participate in SG/TA coordinates "
            + "or zone classification.",
        implementation = NorthAmericanTanninLevel.class,
        example = "MEDIUM",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable NorthAmericanTanninLevel northAmericanTanninLevel,
    @Schema(description = "Computed graph point coordinates.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityGraphPointResponse point
) {
}
