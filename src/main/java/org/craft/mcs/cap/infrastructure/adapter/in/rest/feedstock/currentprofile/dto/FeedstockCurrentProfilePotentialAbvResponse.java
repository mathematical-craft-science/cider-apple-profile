package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.currentprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto.ProfileCharacteristicValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential ABV view for the current feedstock batch measurement.")
public record FeedstockCurrentProfilePotentialAbvResponse(
    @Schema(
        description = "Value kind of original gravity used for prediction.",
        implementation = ProfileCharacteristicValueKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileCharacteristicValueKind originalGravityValueKind,
    @Schema(description = "Potential ABV prediction.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PotentialAbvPredictionResponse prediction
) {
}
