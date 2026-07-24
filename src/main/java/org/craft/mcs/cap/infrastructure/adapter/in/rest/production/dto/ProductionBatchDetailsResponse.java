package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchLifecycle;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto.ChemicalProfileResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Production batch details without traceability graph.")
public record ProductionBatchDetailsResponse(
    @Schema(description = "Production batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID batchId,
    @Schema(description = "Workspace identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID workspaceId,
    @Schema(description = "Production batch name.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String name,
    @Schema(
        description = "Current vessel, tank or container.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String vessel,
    @Schema(
        description = "Current material lifecycle.",
        implementation = ProductionBatchLifecycle.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProductionBatchLifecycle lifecycle,
    @Schema(
        description = "Current state of the primary alcoholic fermentation cycle.",
        implementation = AlcoholicFermentationState.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AlcoholicFermentationState alcoholicFermentationState,
    @Schema(description = "Current production volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse currentVolume,
    @Schema(
        description = "Chemical profile snapshot accepted at batch creation.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull SugarAcidityProfilePointResponse initialProfileSnapshot,
    @Schema(
        description = "Current profile with measured axes overlaid on the immutable initial snapshot.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ChemicalProfileResponse currentChemicalProfile,
    @Schema(
        description = "Measurement records supplying axes of the current profile.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull List<@NonNull MeasurementEvidenceResponse> measurementEvidence,
    @Schema(
        description = "Current yeast-assimilable nitrogen after applying measurement evidence.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse yeastAssimilableNitrogen,
    @Schema(
        description = "Potential ABV prediction calculated from current production profile sugar.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull PotentialAbvPredictionResponse potentialAbv,
    @Schema(description = "Optional production note.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String note
) {
}
