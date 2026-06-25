package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.detail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto.PotentialAbvPredictionResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


/**
 * Transport wrapper for potential ABV prediction and the gravity value kind behind it.
 */
@Schema(description = "Potential ABV prediction for the feedstock detail current profile.")
public record FeedstockBatchDetailPotentialAbvResponse(
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
