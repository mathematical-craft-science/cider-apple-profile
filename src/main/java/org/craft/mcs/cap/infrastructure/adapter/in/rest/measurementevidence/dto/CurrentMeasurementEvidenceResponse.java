package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurementevidence.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSubjectResponse;
import org.jspecify.annotations.NonNull;


/** Transport projection of all measurement evidence applicable to one subject's current state. */
@Schema(
    description = "State-aware measurement evidence currently applicable to one measurable subject."
)
public record CurrentMeasurementEvidenceResponse(
    @Schema(
        description = "Business subject whose measurement evidence is returned.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSubjectResponse subject,
    @Schema(
        description = "Current material-state identifier defining the evidence applicability scope.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Current evidence selected independently per characteristic by material-state lineage.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = CurrentMeasurementCharacteristicResponse.class)
    )
    @NonNull List<@NonNull CurrentMeasurementCharacteristicResponse> characteristics
) {
}
