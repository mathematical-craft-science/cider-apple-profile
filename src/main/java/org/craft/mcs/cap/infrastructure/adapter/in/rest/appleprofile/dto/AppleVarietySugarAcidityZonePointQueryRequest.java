package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.jspecify.annotations.Nullable;


@Schema(description = "Query request for apple variety sugar-acidity graph points.")
public record AppleVarietySugarAcidityZonePointQueryRequest(
    @Schema(description = "Optional case-insensitive name fragment filter.", example = "kingston")
    @Nullable String nameContains,
    @Schema(description = "Optional sugar-acidity zone filter.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES")
    @Nullable SugarAcidityZone zone,
    @Schema(description = "Optional chemical profile capability filter.", implementation = ChemicalProfileCapability.class, example = "SUGAR_ACIDITY_PLOT")
    @Nullable ChemicalProfileCapability capability,
    @Schema(description = "Whether the optional capability must be met.", example = "true")
    @Nullable Boolean capable,
    @Schema(description = "Opaque cursor returned by the previous page.")
    @Nullable String after,
    @Schema(description = "Maximum number of results.", example = "50")
    @Nullable Integer limit
) {
}
