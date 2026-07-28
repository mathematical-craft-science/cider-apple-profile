package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Typed characteristics supplied to one profile-target assessment. */
@Schema(description = "Typed characteristics supplied to profile-target scoring.")
public record ProfileTargetScoringCharacteristicsResponse(
    @Schema(description = "Specific Gravity (SG).", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileCharacteristicValueResponse specificGravity,
    @Schema(description = "Titratable Acidity (TA).", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull ProfileCharacteristicValueResponse titratableAcidity,
    @Schema(
        description = "Optional tannin used by the target objective.",
        nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable ProfileCharacteristicValueResponse tannin
) {
}
