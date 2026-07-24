package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Measurement sets associated with one business subject.")
public record MeasurementSetListResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Current revisions of the subject's logical measurement sets.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementSetResponse.class)
    )
    @NonNull List<@NonNull MeasurementSetResponse> items
) {
}
