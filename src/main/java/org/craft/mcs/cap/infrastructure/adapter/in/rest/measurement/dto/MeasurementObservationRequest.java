package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.jspecify.annotations.NonNull;


@Schema(description = "One point observation retained as metrological evidence.")
public record MeasurementObservationRequest(
    @Schema(
        description = "Observed point value accepted with an explicit input unit.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull PointMeasurementValueRequest value,
    @Schema(
        description = "Time when this individual observation was made.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Source, method, reference, and confidence of this observation.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementProvenanceRequest provenance
) {
}
