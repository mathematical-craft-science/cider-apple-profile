package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZone;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request for current feedstock batch sugar-acidity graph points.")
public record FeedstockBatchCurrentSugarAcidityZonePointQueryRequest(
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff")
    UUID ciderMakerId,
    @Schema(description = "Optional feedstock batch identifier filter.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f")
    @Nullable UUID batchId,
    @Schema(description = "Optional season overlap filter.")
    @Nullable FeedstockSeasonRequest season,
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
