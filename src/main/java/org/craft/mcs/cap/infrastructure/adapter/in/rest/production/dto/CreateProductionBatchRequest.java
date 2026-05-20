package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Request to create a production batch from an accepted production input.")
public record CreateProductionBatchRequest(
    @Schema(description = "Cider maker identifier.", format = "uuid", example = "4d96759c-86ff-4d39-8b19-241bc60f82ff")
    @NonNull UUID ciderMakerId,
    @Schema(description = "Production batch name.", example = "October cider base")
    @NonNull String name,
    @Schema(description = "Production batch kind.", implementation = ProductionBatchKind.class, example = "CIDER")
    @NonNull ProductionBatchKind kind,
    @ArraySchema(schema = @Schema(description = "Accepted feedstock source.", implementation = CreateProductionBatchSourceRequest.class))
    @NonNull List<@NonNull CreateProductionBatchSourceRequest> sources,
    @Schema(description = "Accepted production input profile in canonical service units.")
    @NonNull ProductionProfileSnapshotRequest profile,
    @Schema(description = "Optional accepted distance to the target calculated before production creation.", example = "0.125")
    @Nullable BigDecimal targetDistance,
    @Schema(description = "Optional production note.", example = "Planned first fermentation in tank A.")
    @Nullable String note
) {
}
