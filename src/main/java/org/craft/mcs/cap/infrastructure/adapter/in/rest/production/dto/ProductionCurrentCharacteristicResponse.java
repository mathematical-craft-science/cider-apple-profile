package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionCharacteristicOrigin;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementEvidenceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "One current Production characteristic with its authoritative source.")
public record ProductionCurrentCharacteristicResponse(
    @Schema(description = "Current canonical value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileCharacteristicValueResponse value,
    @Schema(
        description = "Whether the value comes from the initial calculated snapshot or measurement evidence.",
        implementation = ProductionCharacteristicOrigin.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProductionCharacteristicOrigin origin,
    @Schema(
        description = "Measurement evidence when origin is MEASUREMENT.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable MeasurementEvidenceResponse evidence
) {
}
