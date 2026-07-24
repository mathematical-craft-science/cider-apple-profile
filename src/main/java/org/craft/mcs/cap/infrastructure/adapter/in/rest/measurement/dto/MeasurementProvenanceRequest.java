package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Source and confidence evidence for one measurement record.")
public record MeasurementProvenanceRequest(
    @Schema(
        description = "Category of the authority or mechanism that supplied the measured value.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSourceType sourceType,
    @Schema(
        description = "Optional human-readable laboratory, instrument, institute, user, or dataset name.",
        minLength = 1,
        maxLength = 255,
        nullable = true
    )
    @Nullable String sourceName,
    @Schema(
        description = "Optional analytical or measurement method used by the source.",
        minLength = 1,
        maxLength = 255,
        nullable = true
    )
    @Nullable String method,
    @Schema(
        description = "Optional opaque source reference such as a report number, document citation, or URL.",
        minLength = 1,
        maxLength = 1000,
        nullable = true
    )
    @Nullable String reference,
    @Schema(
        description = "Optional declared confidence; null means that confidence was not provided.",
        nullable = true
    )
    @Nullable ConfidenceLevel confidence
) {
}
