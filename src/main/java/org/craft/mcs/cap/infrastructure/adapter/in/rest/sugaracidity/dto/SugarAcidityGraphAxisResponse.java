package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Sugar-acidity graph axis metadata.")
public record SugarAcidityGraphAxisResponse(
    @Schema(description = "Axis code.", example = "SG", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String code,
    @Schema(description = "Human-readable axis label.", example = "Specific gravity", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String label,
    @Schema(description = "Axis unit.", example = "SG", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String unit,
    @Schema(description = "Minimum axis value.", example = "1.000", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal min,
    @Schema(description = "Maximum axis value.", example = "1.080", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal max,
    @ArraySchema(
        arraySchema = @Schema(description = "Threshold line values.", requiredMode = Schema.RequiredMode.REQUIRED),
        schema = @Schema(example = "1.045")
    )
    @NonNull List<@NonNull BigDecimal> thresholdLines
) {
}
