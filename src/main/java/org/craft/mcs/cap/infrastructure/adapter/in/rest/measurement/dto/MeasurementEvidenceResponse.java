package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.core.measurement.application.model.MeasurementFreshnessStatus;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;


@Schema(
    description = "Reference and provenance of the immutable RESULT selected for one current characteristic."
)
public record MeasurementEvidenceResponse(
    @Schema(
        description = "Characteristic for which this evidence was selected.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(
        description = "Stable logical measurement-set identifier containing the selected record.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementSetId,
    @Schema(
        description = "Identifier of the immutable RESULT record selected as current evidence.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID recordId,
    @Schema(
        description = "Time of the selected measurement result.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Freshness of this characteristic relative to workspace policy.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessStatus freshnessStatus,
    @Schema(
        description = "Source and confidence evidence of the selected result.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementProvenanceResponse provenance
) {
}
