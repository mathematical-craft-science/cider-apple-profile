package org.craft.mcs.cap.infrastructure.adapter.in.rest.vessel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.FORMAT;
import static org.craft.mcs.cap.shared.infrastructure.rest.UuidV7Schema.PATTERN;


/** Compact vessel reference embedded in material batch responses. */
@Schema(description = "Stable vessel identity with display data embedded in another read model.")
public record VesselReferenceResponse(
    @Schema(
        description = "Stable UUIDv7 vessel identifier.",
        format = FORMAT,
        pattern = PATTERN,
        example = "019fd42c-3ac8-7000-91fe-48173dfc0c7e",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID vesselId,
    @Schema(
        description = "Current catalog name resolved when the read model was built.",
        example = "Fermentation tank F-03",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String name
) {
}
