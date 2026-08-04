package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Current Production characteristics composed from initial and measured values.")
public record ProductionCurrentProfileResponse(
    @Schema(description = "Current Specific Gravity.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProductionCurrentCharacteristicResponse specificGravity,
    @Schema(description = "Current Titratable Acidity.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProductionCurrentCharacteristicResponse titratableAcidity,
    @Schema(description = "Current pH.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProductionCurrentCharacteristicResponse ph,
    @Schema(description = "Current tannin.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProductionCurrentCharacteristicResponse tannin,
    @Schema(description = "Current YAN.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable ProductionCurrentCharacteristicResponse yeastAssimilableNitrogen
) {
}
