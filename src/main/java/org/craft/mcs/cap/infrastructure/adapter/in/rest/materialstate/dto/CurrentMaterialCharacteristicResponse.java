package org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.craft.mcs.cap.shared.application.measurement.MeasurementValueOrigin;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceValidity;
import org.craft.mcs.cap.shared.application.measurement.MeasurementFreshnessStatus;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementProvenanceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementValueResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Current characteristic value with immutable measurement evidence and state origin.")
public record CurrentMaterialCharacteristicResponse(
    @Schema(
        description = "Measured characteristic.",
        implementation = MeasurementCharacteristic.class,
        example = "SPECIFIC_GRAVITY",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(description = "Canonical point or interval value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueResponse value,
    @Schema(
        description = "How the characteristic entered the current material state.",
        implementation = MeasurementValueOrigin.class,
        example = "MEASURED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueOrigin origin,
    @Schema(
        description = "Material state in which this evidence originated.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID evidenceMaterialStateId,
    @Schema(
        description = "Logical measurement-set identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID measurementSetId,
    @Schema(
        description = "Immutable measurement-record identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID recordId,
    @Schema(description = "Time at which this characteristic was measured.", format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull Instant measuredAt,
    @Schema(
        description = "Source and confidence of the selected evidence.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementProvenanceResponse provenance,
    @Schema(
        description = "Freshness relative to workspace policy.",
        implementation = MeasurementFreshnessStatus.class,
        example = "CURRENT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementFreshnessStatus freshnessStatus,
    @Schema(
        description = "Whether this evidence remains applicable.",
        implementation = MeasurementEvidenceValidity.class,
        example = "VALID",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementEvidenceValidity validity
) {
}
