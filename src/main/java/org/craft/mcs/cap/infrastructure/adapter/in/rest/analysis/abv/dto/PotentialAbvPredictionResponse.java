package org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Potential alcohol by volume prediction.")
public record PotentialAbvPredictionResponse(
    @Schema(
        description = "Original gravity used for prediction.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse originalGravity,
    @Schema(
        description = "Predicted final gravity used for prediction.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse predictedFinalGravity,
    @Schema(
        description = "Source of predicted final gravity assumption.",
        implementation = FinalGravitySource.class,
        example = "FIXED_DRY_CIDER_DEFAULT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull FinalGravitySource predictedFinalGravitySource,
    @Schema(
        description = "Potential ABV percentage.",
        requiredMode = Schema.RequiredMode.REQUIRED
    ) @NonNull DecimalQuantityResponse potentialAbv,
    @Schema(
        description = "Prediction method code.",
        implementation = Method.class,
        example = "STANDARD_GRAVITY_DROP",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Method method,
    @Schema(
        description = "Prediction confidence code.",
        implementation = Confidence.class,
        example = "LOW",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Confidence confidence
) {

  public enum FinalGravitySource {
    FIXED_DRY_CIDER_DEFAULT,
    YEAST_CHARACTERISTICS,
    FERMENTATION_MODEL,
    FERMENTATION_MEASUREMENTS
  }

  public enum Method {
    STANDARD_GRAVITY_DROP
  }

  public enum Confidence {
    LOW,
    MEDIUM,
    HIGH
  }

}
