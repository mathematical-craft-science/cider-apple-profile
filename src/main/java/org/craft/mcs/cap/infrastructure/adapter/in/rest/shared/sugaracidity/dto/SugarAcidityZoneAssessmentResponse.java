package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityLevel;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Sugar-acidity zone assessment with graph coordinates.")
public record SugarAcidityZoneAssessmentResponse(
    @Schema(description = "Whether the point belongs to a known sugar-acidity zone.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean classified,
    @Schema(description = "Zone code, when classified.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityZone zone,
    @Schema(description = "Sugar classification term.", implementation = SugarAcidityLevel.class, example = "HIGH", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityLevel sugar,
    @Schema(description = "Acidity classification term.", implementation = SugarAcidityLevel.class, example = "MEDIUM", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull SugarAcidityLevel acidity,
    @Schema(description = "Tannin classification term, when present.", implementation = SugarAcidityLevel.class, example = "MEDIUM", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable SugarAcidityLevel tannin,
    @Schema(description = "Whether this profile is balanced for a single-variety cider.", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean balancedForSingleVariety,
    @Schema(description = "Computed graph point coordinates.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse point
) {
}
