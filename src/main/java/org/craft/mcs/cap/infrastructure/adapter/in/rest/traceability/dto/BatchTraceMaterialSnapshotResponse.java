package org.craft.mcs.cap.infrastructure.adapter.in.rest.traceability.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Material state captured by a trace step.")
public record BatchTraceMaterialSnapshotResponse(
    @Schema(
        description = "Volume, when captured.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable DecimalQuantityResponse volume,
    @ArraySchema(
        arraySchema = @Schema(
            description = "Typed material characteristics captured in CAP canonical units.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = BatchTraceCharacteristicResponse.class)
    )
    @NonNull List<@NonNull BatchTraceCharacteristicResponse> characteristics,
    @Schema(
        description = "Confidence level of captured profile data.",
        implementation = ConfidenceLevel.class,
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ConfidenceLevel confidence
) {

  public BatchTraceMaterialSnapshotResponse {
    characteristics = List.copyOf(characteristics);
  }
}
