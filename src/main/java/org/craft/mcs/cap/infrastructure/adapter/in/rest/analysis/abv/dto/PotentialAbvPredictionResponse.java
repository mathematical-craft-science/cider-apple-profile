package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.AbvPredictionMethod;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.FinalGravityPredictionSource;
import org.craft.mcs.cap.core.cideranalysis.abv.domain.PredictionConfidence;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential alcohol by volume prediction.")
public record PotentialAbvPredictionResponse(
    @Schema(description = "Original gravity used for prediction.", example = "1.052", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal originalGravity,
    @Schema(description = "Predicted final gravity used for prediction.", example = "1.000", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal predictedFinalGravity,
    @Schema(description = "Source of predicted final gravity assumption.", implementation = FinalGravityPredictionSource.class, example = "FIXED_DRY_CIDER_DEFAULT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull FinalGravityPredictionSource predictedFinalGravitySource,
    @Schema(description = "Potential ABV percentage.", example = "6.83", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal potentialAbv,
    @Schema(description = "Prediction method code.", implementation = AbvPredictionMethod.class, example = "STANDARD_GRAVITY_DROP", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull AbvPredictionMethod method,
    @Schema(description = "Prediction confidence code.", implementation = PredictionConfidence.class, example = "LOW", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull PredictionConfidence confidence
) {
}
