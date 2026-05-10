package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;


@Schema(description = "Sugar-acidity graph axis metadata.")
public record SugarAcidityGraphAxisResponse(
    @Schema(description = "Axis code.", example = "SG") @NonNull String code,
    @Schema(description = "Human-readable axis label.", example = "Specific gravity") @NonNull String label,
    @Schema(description = "Axis unit.", example = "SG") @NonNull String unit,
    @Schema(description = "Minimum axis value.", example = "1.000") @NonNull BigDecimal min,
    @Schema(description = "Maximum axis value.", example = "1.080") @NonNull BigDecimal max,
    @ArraySchema(schema = @Schema(description = "Threshold line value.", example = "1.045"))
    @NonNull List<@NonNull BigDecimal> thresholdLines
) {
}
