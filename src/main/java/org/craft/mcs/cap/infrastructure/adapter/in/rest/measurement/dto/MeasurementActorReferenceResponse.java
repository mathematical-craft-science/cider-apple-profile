package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.shared.kernel.identity.ActorType;
import org.jspecify.annotations.NonNull;


@Schema(description = "Provider-neutral CAP actor that created an immutable measurement revision.")
public record MeasurementActorReferenceResponse(
    @Schema(
        description = "Actor category used by CAP audit semantics.",
        implementation = ActorType.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ActorType type,
    @Schema(
        description = "Provider-neutral CAP actor identifier.",
        format = "uuid",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull UUID id
) {
}
