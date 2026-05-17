package org.craft.mcs.cap.infrastructure.adapter.in.rest.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


@Schema(description = "Source weight, either absolute volume in liters or relative proportion.")
public record ChemicalProfileCompositionSourceWeightRequest(
    @Schema(description = "Weight kind.", implementation = ChemicalProfileCompositionSourceWeightKind.class, example = "ABSOLUTE_VOLUME_LITERS")
    @NonNull ChemicalProfileCompositionSourceWeightKind kind,
    @Schema(description = "Weight value. Liters for ABSOLUTE_VOLUME_LITERS, 0..1 proportion for PROPORTION.", example = "10")
    @NonNull BigDecimal value
) {
}
