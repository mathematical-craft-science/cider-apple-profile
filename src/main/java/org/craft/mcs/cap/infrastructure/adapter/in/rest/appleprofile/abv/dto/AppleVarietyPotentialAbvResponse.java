package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.abv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential ABV prediction response for an apple variety reference profile.")
public record AppleVarietyPotentialAbvResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID varietyId,
    @Schema(
        description = "Apple variety name.",
        example = "Kingston Black",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String varietyName,
    @Schema(
        description = "Source value kind used for original gravity.",
        implementation = AxisValueKind.class,
        example = "POINT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AxisValueKind originalGravityValueKind,
    @Schema(description = "Potential ABV prediction.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PotentialAbvPredictionResponse prediction
) {
}
