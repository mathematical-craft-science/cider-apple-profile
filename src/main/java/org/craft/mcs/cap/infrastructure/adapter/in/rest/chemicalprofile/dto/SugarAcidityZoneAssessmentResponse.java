package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityLevel;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Sugar-acidity zone assessment with graph coordinates.")
public record SugarAcidityZoneAssessmentResponse(
    @Schema(description = "Whether the point belongs to a known sugar-acidity zone.", example = "true")
    boolean classified,
    @Schema(description = "Zone code, when classified.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES")
    @Nullable SugarAcidityZone zone,
    @Schema(description = "Sugar classification term.", implementation = SugarAcidityLevel.class, example = "HIGH") @NonNull SugarAcidityLevel sugar,
    @Schema(description = "Acidity classification term.", implementation = SugarAcidityLevel.class, example = "MEDIUM") @NonNull SugarAcidityLevel acidity,
    @Schema(description = "Tannin classification term, when present.", implementation = SugarAcidityLevel.class, example = "MEDIUM") @Nullable SugarAcidityLevel tannin,
    @Schema(description = "Whether this profile is balanced for a single-variety cider.", example = "true")
    boolean balancedForSingleVariety,
    @Schema(description = "Computed graph point coordinates.")
    @NonNull SugarAcidityProfilePointResponse point
) {
}
