package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;


@Schema(description = "Reference to an external or domain source used in generic composition calculation.")
public record ChemicalProfileCompositionSourceReferenceRequest(
    @Schema(description = "Source type.", example = "FEEDSTOCK_BATCH", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String type,
    @Schema(description = "Source identifier. Usually UUID for persisted domain sources.", example = "b2a49b8a-3a19-4f8f-989d-9278f4e00d2f", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String id
) {
}
