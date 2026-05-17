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
    @ArraySchema(schema = @Schema(description = "Production source contribution.", implementation = ProductionBatchSourceResponse.class))
    @NonNull List<@NonNull ProductionBatchSourceResponse> sources,
    @Schema(description = "Calculated production profile point.")
    @NonNull SugarAcidityProfilePointResponse profile,
    @Schema(description = "Total production volume in liters.", example = "100.0")
    @NonNull BigDecimal totalVolumeLiters,
    @Schema(description = "Accepted distance to target, when calculated before production creation.", example = "0.125")
    @Nullable BigDecimal targetDistance
) {
}
