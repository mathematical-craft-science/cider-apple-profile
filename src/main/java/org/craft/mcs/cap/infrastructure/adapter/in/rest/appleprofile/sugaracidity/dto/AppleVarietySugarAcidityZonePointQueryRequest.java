package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.cideranalysis.profileaxis.domain.ChemicalProfileCapability;
import org.jspecify.annotations.Nullable;


@Schema(description = "Query request for apple variety sugar-acidity graph points.")
public record AppleVarietySugarAcidityZonePointQueryRequest(
    @Schema(description = "Optional case-insensitive name fragment filter.", example = "kingston", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String nameContains,
    @Schema(description = "Optional sugar-acidity zone filter.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable SugarAcidityZone zone,
    @Schema(description = "Optional chemical profile capability filter.", implementation = ChemicalProfileCapability.class, example = "SUGAR_ACIDITY_PLOT", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ChemicalProfileCapability capability,
    @Schema(description = "Whether the optional capability must be met.", example = "true", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Boolean capable,
    @Schema(description = "Opaque cursor returned by the previous page.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String after,
    @Schema(description = "Maximum number of results.", example = "50", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Integer limit
) {
}
