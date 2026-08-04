package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.profileanalysis.capability.domain.ProfileAnalysisCapability;
import org.jspecify.annotations.Nullable;


@Schema(description = "Query request for apple variety sugar-acidity graph points.")
public record AppleVarietySugarAcidityZonePointQueryRequest(
    @Schema(
        description = "Optional case-insensitive name fragment filter.",
        example = "kingston",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String nameContains,
    @Schema(
        description = "Optional Jolicoeur sugar-acidity zone filter.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Optional profile-analysis capability filter.",
        implementation = ProfileAnalysisCapability.class,
        example = "SUGAR_ACIDITY_GRAPH",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileAnalysisCapability capability,
    @Schema(
        description = "Whether the optional capability must be met.",
        example = "true",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Boolean capable,
    @Schema(
        description = "Opaque cursor returned by the previous page.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable String after,
    @Schema(
        description = "Maximum number of results.",
        example = "50",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Integer limit
) {
}
