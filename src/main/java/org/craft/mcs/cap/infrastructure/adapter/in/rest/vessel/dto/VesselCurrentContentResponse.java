package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.vessel.application.model.VesselCurrentContentType;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.FORMAT;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;


/** Active material batch currently occupying one vessel. */
@Schema(description = "Single active material batch currently assigned to the vessel.")
public record VesselCurrentContentResponse(
    @Schema(
        description = "Bounded-context type of the current material batch.",
        implementation = VesselCurrentContentType.class,
        example = "FEEDSTOCK_BATCH",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull VesselCurrentContentType subjectType,
    @Schema(
        description = "Stable UUIDv7 identifier of the Feedstock or Production batch.",
        format = FORMAT,
        pattern = PATTERN,
        example = "019fd42c-3ac8-7000-91fe-48173dfc0c7f",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID subjectId,
    @Schema(
        description = "UUIDv7 identifier of the batch's current immutable material state.",
        format = FORMAT,
        pattern = PATTERN,
        example = "019fd42c-3ac8-7000-91fe-48173dfc0c80",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID materialStateId,
    @Schema(
        description = "Current batch name for workspace display.",
        example = "October Kingston Black juice",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String displayName,
    @Schema(
        description = "Current material volume in canonical volume units.",
        implementation = DecimalQuantityResponse.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse volume
) {
}
