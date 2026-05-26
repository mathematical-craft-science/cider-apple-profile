package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Production batch list response.")
public record ProductionBatchListResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "Production batches.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = ProductionBatchDetailsResponse.class)
    )
    @NonNull List<@NonNull ProductionBatchDetailsResponse> batches
) {

  public ProductionBatchListResponse {
    batches = List.copyOf(batches);
  }
}
