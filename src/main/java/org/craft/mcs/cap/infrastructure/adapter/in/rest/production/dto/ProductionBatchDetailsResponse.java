package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
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
    @Schema(description = "Current production volume in liters.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal volumeLiters,
    @Schema(description = "Production profile snapshot.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(description = "Accepted distance to target, when available.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal targetDistance,
    @Schema(description = "Optional production note.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable String note
) {
}
