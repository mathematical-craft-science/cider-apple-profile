package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
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
        description = "Optional filter by whether SG and TA can be plotted.",
        example = "true",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable Boolean plottable,
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
