package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


/** Workspace-scoped vessel catalog response. */
@Schema(description = "Vessels registered in one workspace catalog.")
public record VesselListResponse(
    @ArraySchema(
        arraySchema = @Schema(
            description = "Vessels ordered by name and stable identity.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = VesselResponse.class)
    )
    @NonNull List<@NonNull VesselResponse> vessels
) {
}
