package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Production batch details without traceability graph.")
public record ProductionBatchDetailsResponse(
    @Schema(description = "Production batch identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID batchId,
    @Schema(description = "Cider maker identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID ciderMakerId,
    @Schema(description = "Production batch name.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String name,
    @Schema(description = "Production batch kind.", implementation = ProductionBatchKind.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProductionBatchKind kind,
    @Schema(description = "Production batch status.", implementation = ProductionBatchStatus.class, requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProductionBatchStatus status,
    @Schema(description = "Current production volume.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse volume,
    @Schema(description = "Production profile snapshot.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(description = "Yeast-assimilable nitrogen from the accepted production profile, when available.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable DecimalQuantityResponse yeastAssimilableNitrogen,
    @Schema(description = "Potential ABV prediction calculated from production profile sugar.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PotentialAbvPredictionResponse potentialAbv,
    @Schema(description = "Accepted distance to target, when available.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal targetDistance,
    @Schema(description = "Optional production note.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String note
) {
}
