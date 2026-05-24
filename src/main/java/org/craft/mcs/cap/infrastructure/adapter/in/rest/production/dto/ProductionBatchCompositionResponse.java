package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Production batch calculated chemical profile composition.")
public record ProductionBatchCompositionResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Production source contributions.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ProductionBatchSourceResponse.class)
    )
    @NonNull List<@NonNull ProductionBatchSourceResponse> sources,
    @Schema(description = "Calculated production profile point.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(description = "Total production volume in liters.", example = "100.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal totalVolumeLiters,
    @Schema(description = "Accepted distance to target, when calculated before production creation.", example = "0.125", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable BigDecimal targetDistance
) {
}
