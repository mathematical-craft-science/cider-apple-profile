package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementSourceType;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Source and trust evidence for one immutable measurement record.")
public record MeasurementProvenanceResponse(
    @Schema(
        description = "Category of the authority or mechanism that supplied the measured value.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSourceType sourceType,
    @Schema(
        description = "Human-readable source name when one was declared.",
        minLength = 1,
        maxLength = 255,
        nullable = true
    )
    @Nullable String sourceName,
    @Schema(
        description = "Analytical or measurement method when one was declared.",
        minLength = 1,
        maxLength = 255,
        nullable = true
    )
    @Nullable String method,
    @Schema(
        description = "Opaque report, document, dataset, or URL reference when one was declared.",
        minLength = 1,
        maxLength = 1000,
        nullable = true
    )
    @Nullable String reference,
    @Schema(
        description = "Declared confidence; null means that confidence was not provided.",
        nullable = true
    )
    @Nullable ConfidenceLevel confidence
) {
}
