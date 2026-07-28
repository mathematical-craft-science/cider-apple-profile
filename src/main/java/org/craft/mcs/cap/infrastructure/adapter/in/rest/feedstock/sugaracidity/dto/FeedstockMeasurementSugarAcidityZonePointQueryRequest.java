package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Query request for historical feedstock measurement sugar-acidity graph points.")
public record FeedstockMeasurementSugarAcidityZonePointQueryRequest(
    @Schema(
        description = "Workspace identifier.",
        format = "uuid",
        example = "4d96759c-86ff-4d39-8b19-241bc60f82ff",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID workspaceId,
    @Schema(
        description = "Optional feedstock batch filter.",
        format = "uuid",
        example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable UUID batchId,
    @Schema(
        description = "Optional season overlap filter.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable FeedstockSeasonRequest season,
    @Schema(
        description = "Optional Jolicoeur sugar-acidity zone filter.",
        implementation = JolicoeurSugarAcidityZone.class,
        example = "PERFECT_APPLES",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable JolicoeurSugarAcidityZone zone,
    @Schema(
        description = "Optional chemical profile capability filter.",
        implementation = ChemicalProfileCapability.class,
        example = "SUGAR_ACIDITY_GRAPH",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ChemicalProfileCapability capability,
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

  public FeedstockMeasurementSugarAcidityZonePointQueryRequest {
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
