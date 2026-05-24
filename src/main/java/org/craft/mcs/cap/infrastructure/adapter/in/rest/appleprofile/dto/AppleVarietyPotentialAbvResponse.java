package org.craft.mcs.cap.infrastructure.adapter.in.rest.appleprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential ABV prediction response for an apple variety reference profile.")
public record AppleVarietyPotentialAbvResponse(
    @Schema(description = "Apple variety identifier.", format = "uuid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull UUID varietyId,
    @Schema(description = "Apple variety name.", example = "Kingston Black", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String varietyName,
    @Schema(description = "Source value kind used for original gravity.", implementation = MeasurementValueKind.class, example = "EXACT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueKind originalGravityValueKind,
    @Schema(description = "Potential ABV prediction.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PotentialAbvPredictionResponse prediction
) {
}
