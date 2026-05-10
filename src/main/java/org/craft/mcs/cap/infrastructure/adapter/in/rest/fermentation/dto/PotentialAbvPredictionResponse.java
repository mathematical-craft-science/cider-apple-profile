package org.craft.mcs.cap.infrastructure.adapter.in.rest.fermentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.domain.fermentation.AbvPredictionMethod;
import org.craft.mcs.cap.shared.domain.fermentation.FinalGravityPredictionSource;
import org.craft.mcs.cap.shared.domain.fermentation.PredictionConfidence;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential alcohol by volume prediction.")
public record PotentialAbvPredictionResponse(
    @Schema(description = "Original gravity used for prediction.", example = "1.052") @NonNull BigDecimal originalGravity,
    @Schema(description = "Predicted final gravity used for prediction.", example = "1.000") @NonNull BigDecimal predictedFinalGravity,
    @Schema(description = "Source of predicted final gravity assumption.", implementation = FinalGravityPredictionSource.class, example = "FIXED_DRY_CIDER_DEFAULT")
    @NonNull FinalGravityPredictionSource predictedFinalGravitySource,
    @Schema(description = "Potential ABV percentage.", example = "6.83") @NonNull BigDecimal potentialAbv,
    @Schema(description = "Prediction method code.", implementation = AbvPredictionMethod.class, example = "STANDARD_GRAVITY_DROP")
    @NonNull AbvPredictionMethod method,
    @Schema(description = "Prediction confidence code.", implementation = PredictionConfidence.class, example = "LOW")
    @NonNull PredictionConfidence confidence
) {
}
