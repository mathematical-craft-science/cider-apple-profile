package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.AbvPredictionMethod;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PredictionConfidence;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential alcohol by volume prediction.")
public record PotentialAbvPredictionResponse(
    @Schema(description = "Original gravity used for prediction.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse originalGravity,
    @Schema(description = "Predicted final gravity used for prediction.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse predictedFinalGravity,
    @Schema(description = "Source of predicted final gravity assumption.", implementation = FinalGravityPredictionSource.class, example = "FIXED_DRY_CIDER_DEFAULT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FinalGravityPredictionSource predictedFinalGravitySource,
    @Schema(description = "Potential ABV percentage.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse potentialAbv,
    @Schema(description = "Prediction method code.", implementation = AbvPredictionMethod.class, example = "STANDARD_GRAVITY_DROP", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull AbvPredictionMethod method,
    @Schema(description = "Prediction confidence code.", implementation = PredictionConfidence.class, example = "LOW", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PredictionConfidence confidence
) {
}
