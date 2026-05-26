package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Production batch source contribution.")
public record ProductionBatchSourceResponse(
    @Schema(description = "Feedstock batch identifier.", format = "uuid", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID feedstockBatchId,
    @Schema(description = "Volume used from the feedstock batch.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse volume
) {
}
