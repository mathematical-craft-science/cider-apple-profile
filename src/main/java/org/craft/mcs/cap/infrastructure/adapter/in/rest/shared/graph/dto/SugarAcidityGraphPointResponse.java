package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.jspecify.annotations.NonNull;


/**
 * Transport projection on the two-dimensional sugar-acidity graph.
 *
 * <p>For interval evidence, each characteristic retains its bounds while its value contains the
 * representative midpoint used as the graph coordinate.</p>
 */
@Schema(
    description = "Projection on the Specific Gravity (SG) and Titratable Acidity (TA) graph. "
        + "Interval evidence retains its bounds and exposes the representative midpoint as value."
)
public record SugarAcidityGraphPointResponse(
    @Schema(description = "Specific Gravity (SG) coordinate.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileCharacteristicValueResponse specificGravity,
    @Schema(description = "Titratable Acidity (TA) coordinate.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileCharacteristicValueResponse titratableAcidity
) {
}
