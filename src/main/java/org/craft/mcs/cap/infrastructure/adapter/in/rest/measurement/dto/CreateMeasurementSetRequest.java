package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(
    description = "Creates a logical measurement set and its first immutable revision for one business subject."
)
public record CreateMeasurementSetRequest(
    @Schema(
        description = "Optional caller-assigned stable identifier of the logical set. CAP generates one when omitted.",
        format = "uuid",
        nullable = true
    )
    @Nullable UUID measurementSetId,
    @Schema(
        description = "Business aggregate whose material state was measured.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementSubjectRequest subject,
    @Schema(
        description = "Current material-state identifier observed before recording this measurement. "
            + "The request fails with 409 when the material has already changed.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID expectedCurrentStateId,
    @Schema(
        description = "Human-readable name of the measurement session or laboratory result.",
        minLength = 1,
        maxLength = 160,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name,
    @Schema(
        description = "Optional note retained with this immutable revision.",
        maxLength = 2000,
        nullable = true
    )
    @Nullable String note,
    @ArraySchema(
        minItems = 1,
        arraySchema = @Schema(
            description = "Distinct measured characteristics included in the initial revision.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementCharacteristicRequest.class)
    )
    @NonNull List<@NonNull MeasurementCharacteristicRequest> measurements
) {
}
