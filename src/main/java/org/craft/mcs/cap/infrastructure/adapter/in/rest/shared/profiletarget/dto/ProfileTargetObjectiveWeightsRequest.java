package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;


@Schema(description = "Axis weights used by profile target objective functions.")
public record ProfileTargetObjectiveWeightsRequest(
    @Schema(description = "Sugar axis weight.", example = "1.0")
    @Nullable BigDecimal sugar,
    @Schema(description = "Acidity axis weight.", example = "1.0")
    @Nullable BigDecimal acidity,
    @Schema(description = "Tannin axis weight.", example = "0.5")
    @Nullable BigDecimal tannin
) {
}
