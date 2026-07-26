package org.craft.mcs.cap.infrastructure.adapter.in.rest.materialstate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto.MeasurementSubjectResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Authoritative current immutable state of one measurable material subject.")
public record CurrentMaterialStateResponse(
    @Schema(
        description = "Business subject whose material state is returned.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSubjectResponse subject,
    @Schema(
        description = "Current immutable material-state identifier used to bind new evidence to the sampled state.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID currentMaterialStateId,
    @Schema(
        description = "Characteristics applicable to the current state, selected independently by lineage.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull List<@NonNull CurrentMaterialCharacteristicResponse> characteristics
) {
}
