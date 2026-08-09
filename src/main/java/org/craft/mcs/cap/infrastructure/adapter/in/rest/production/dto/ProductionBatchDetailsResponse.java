package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CalculatedCompositionCharacteristicsResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto.VesselReferenceResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Production batch details without traceability graph.")
public record ProductionBatchDetailsResponse(
    @Schema(description = "Production batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID batchId,
    @Schema(description = "Workspace identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID workspaceId,
    @Schema(
        description = "Current immutable state of the production material.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(description = "Production batch name.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String name,
    @Schema(
        description = "Current vessel reference resolved from the workspace vessel catalog.",
        implementation = VesselReferenceResponse.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull VesselReferenceResponse vessel,
    @Schema(
        description = "Current material lifecycle.",
        implementation = BatchLifecycle.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BatchLifecycle lifecycle,
    @Schema(
        description = "Current state of the primary alcoholic fermentation cycle.",
        implementation = AlcoholicFermentationState.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AlcoholicFermentationState alcoholicFermentationState,
    @Schema(description = "Current production volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse currentVolume,
    @Schema(
        description = "Calculated characteristic snapshot accepted at batch creation.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull CalculatedCompositionCharacteristicsResponse initialProfileSnapshot,
    @Schema(
        description = "Current typed characteristics with per-characteristic origin and evidence.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProductionCurrentProfileResponse currentProfile,
    @Schema(
        description = "Potential ABV prediction calculated from current production profile SG.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull PotentialAbvPredictionResponse potentialAbv,
    @Schema(description = "Optional production note.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String note
) {
}
