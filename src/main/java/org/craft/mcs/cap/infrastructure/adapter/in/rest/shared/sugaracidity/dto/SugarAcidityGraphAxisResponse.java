package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Sugar-acidity graph axis metadata.")
public record SugarAcidityGraphAxisResponse(
    @Schema(description = "Axis code.", example = "SG", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String code,
    @Schema(description = "Human-readable axis label.", example = "Specific gravity", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String label,
    @Schema(description = "Axis unit.", example = "SG", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String unit,
    @Schema(description = "Minimum axis value.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse min,
    @Schema(description = "Maximum axis value.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse max,
    @ArraySchema(
        arraySchema = @Schema(description = "Threshold line values.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(implementation = DecimalQuantityResponse.class)
    )
    @NonNull List<@NonNull DecimalQuantityResponse> thresholdLines
) {
}
