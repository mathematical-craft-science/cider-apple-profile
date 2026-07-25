package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;


@Schema(
    description = "Reference and provenance of the immutable RESULT selected for one current characteristic."
)
public record MeasurementEvidenceResponse(
    @Schema(
        description = "Characteristic for which this evidence was selected.",
        implementation = MeasurementCharacteristic.class,
        example = "SPECIFIC_GRAVITY",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(
        description = "How the characteristic entered the current material state.",
        implementation = MeasurementValueOrigin.class,
        example = "MEASURED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueOrigin origin,
    @Schema(
        description = "Material state in which this characteristic value originated.",
        format = "uuid",
        example = "019f818a-7008-7000-9ef5-3381945e3c41",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID materialStateId,
    @Schema(
        description = "Stable logical measurement-set identifier containing the selected record.",
        format = "uuid",
        example = "019f818b-0134-7000-8af2-a9e37c4b1092",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementSetId,
    @Schema(
        description = "Identifier of the immutable RESULT record selected as current evidence.",
        format = "uuid",
        example = "019f818b-2631-7000-92a0-2cff5a3cb2fd",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID recordId,
    @Schema(
        description = "Time of the selected measurement result.",
        format = "date-time",
        example = "2026-07-25T16:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Freshness of this characteristic relative to workspace policy.",
        implementation = MeasurementFreshnessStatus.class,
        example = "CURRENT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessStatus freshnessStatus,
    @Schema(
        description = "Source and confidence evidence of the selected result.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementProvenanceResponse provenance,
    @Schema(
        description = "Whether the selected evidence remains applicable to the current material lineage.",
        implementation = MeasurementEvidenceValidity.class,
        example = "VALID",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementEvidenceValidity validity
) {
}
