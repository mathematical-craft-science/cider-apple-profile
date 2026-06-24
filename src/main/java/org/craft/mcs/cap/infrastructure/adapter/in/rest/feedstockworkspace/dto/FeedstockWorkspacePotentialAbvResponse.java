package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstockworkspace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential ABV view for the current feedstock batch measurement.")
public record FeedstockWorkspacePotentialAbvResponse(
    @Schema(
        description = "Value kind of original gravity used for prediction.",
        implementation = MeasurementValueKind.class,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind originalGravityValueKind,
    @Schema(description = "Potential ABV prediction.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PotentialAbvPredictionResponse prediction
) {
}
