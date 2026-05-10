package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


@Schema(description = "Closed numeric range on a sugar-acidity graph axis.")
public record SugarAcidityGraphRangeResponse(
    @Schema(description = "Range minimum.", example = "4.5") @NonNull BigDecimal min,
    @Schema(description = "Range maximum.", example = "7.5") @NonNull BigDecimal max
) {
}
