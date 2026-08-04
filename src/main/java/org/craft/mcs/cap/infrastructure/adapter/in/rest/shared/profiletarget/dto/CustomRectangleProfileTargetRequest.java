package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Custom rectangular sugar-acidity target.")
public record CustomRectangleProfileTargetRequest(
    @Schema(
        description = "Target kind.",
        type = "string",
        allowableValues = "CUSTOM_RECTANGLE",
        example = "CUSTOM_RECTANGLE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String kind,
    @Schema(
        description = "Minimum Specific Gravity (SG) coordinate.",
        example = "1.060",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal specificGravityMin,
    @Schema(
        description = "Maximum Specific Gravity (SG) coordinate.",
        example = "1.072",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal specificGravityMax,
    @Schema(
        description = "Minimum Titratable Acidity (TA) in g/L as malic acid.",
        example = "4.5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal titratableAcidityMin,
    @Schema(
        description = "Maximum Titratable Acidity (TA) in g/L as malic acid.",
        example = "7.5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal titratableAcidityMax
) implements ProfileTargetRequest {

  public CustomRectangleProfileTargetRequest {
    requireNonNull(kind, "target kind cannot be null");
    requireNonNull(specificGravityMin, "target specific gravity minimum cannot be null");
    requireNonNull(specificGravityMax, "target specific gravity maximum cannot be null");
    requireNonNull(titratableAcidityMin, "target titratable acidity minimum cannot be null");
    requireNonNull(titratableAcidityMax, "target titratable acidity maximum cannot be null");
  }
}
