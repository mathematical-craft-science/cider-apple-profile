package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSubjectType;
import org.jspecify.annotations.NonNull;


@Schema(description = "Business aggregate whose material state was measured.")
public record MeasurementSubjectRequest(
    @Schema(
        description = "Supported aggregate type of the measured subject.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSubjectType type,
    @Schema(
        description = "Stable business identifier of the measured aggregate.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID id
) {
}
