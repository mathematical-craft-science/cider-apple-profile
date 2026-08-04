package org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;


@Schema(description = "Typed material characteristic captured in an immutable trace snapshot.")
public record BatchTraceCharacteristicResponse(
    @Schema(
        description = "Measured material characteristic represented by the snapshot value.",
        implementation = MeasurementCharacteristic.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(
        description = "Point, interval, or calculated characteristic value in CAP canonical units.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileCharacteristicValueResponse value
) {
}
