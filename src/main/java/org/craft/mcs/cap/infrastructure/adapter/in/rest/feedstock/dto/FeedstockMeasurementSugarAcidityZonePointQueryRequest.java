package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Query request for historical feedstock measurement sugar-acidity graph points.")
public record FeedstockMeasurementSugarAcidityZonePointQueryRequest(
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID ciderMakerId,
    @Schema(description = "Optional feedstock batch filter.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable UUID batchId,
    @Schema(description = "Optional season overlap filter.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable FeedstockSeasonRequest season,
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

  public FeedstockMeasurementSugarAcidityZonePointQueryRequest {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
  }
}
