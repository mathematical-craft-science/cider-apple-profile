package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Production batch response.")
public record ProductionBatchResponse(
    @Schema(description = "Production batch identifier.", format = "uuid", example = "8a9813cf-bc41-4d3f-82a9-51c5f00d4f87")
    @NonNull UUID batchId,
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff")
    @NonNull UUID ciderMakerId,
    @Schema(description = "Production batch name.", example = "October cider base")
    @NonNull String name,
    @Schema(description = "Production batch kind.", implementation = ProductionBatchKind.class, example = "CIDER")
    @NonNull ProductionBatchKind kind,
    @Schema(description = "Production batch status.", implementation = ProductionBatchStatus.class, example = "PLANNED")
    @NonNull ProductionBatchStatus status,
    @Schema(description = "Calculated source composition snapshot.")
    @NonNull ProductionBatchCompositionResponse composition,
    @Schema(description = "Optional production note.", example = "Planned first fermentation in tank A.")
    @Nullable String note
) {
}
