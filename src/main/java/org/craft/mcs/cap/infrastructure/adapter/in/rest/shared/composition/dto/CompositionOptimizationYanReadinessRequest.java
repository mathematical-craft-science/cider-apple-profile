package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


@Schema(description = "Optional YAN readiness criterion for composition optimization.")
public record CompositionOptimizationYanReadinessRequest(
    @Schema(
        description = "Minimum acceptable YAN in mg N/L.", example = "140.0", nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal minimum,
    @Schema(
        description = "Maximum acceptable YAN in mg N/L.", example = "350.0", nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal maximum,
    @Schema(
        description = "Criterion weight applied to proportional YAN penalty.", example = "1.0", nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal weight,
    @Schema(
        description = "Policy used when calculated candidate YAN is missing.",
        implementation = CompositionOptimizationMissingPolicy.class, nullable = true,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable CompositionOptimizationMissingPolicy missingPolicy,
    @Schema(
        description = "Base penalty used when YAN is missing and missingPolicy is PENALIZE_IF_MISSING.",
        example = "0.25", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Nullable BigDecimal missingPenalty
) {

  public static final @NonNull BigDecimal DEFAULT_WEIGHT = BigDecimal.ONE;
  public static final @NonNull BigDecimal DEFAULT_MISSING_PENALTY = new BigDecimal("0.25");
  public static final @NonNull CompositionOptimizationMissingPolicy DEFAULT_MISSING_POLICY =
      CompositionOptimizationMissingPolicy.PENALIZE_IF_MISSING;
}
