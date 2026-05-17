package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Custom rectangular sugar-acidity target.")
public record CustomRectangleProfileTargetRequest(
    @Schema(description = "Target shape.", example = "CUSTOM_RECTANGLE")
    @NonNull String kind,
    @Schema(description = "Minimum sugar coordinate in SG.", example = "1.060")
    @NonNull BigDecimal sugarMin,
    @Schema(description = "Maximum sugar coordinate in SG.", example = "1.072")
    @NonNull BigDecimal sugarMax,
    @Schema(description = "Minimum titratable acidity coordinate in g/L as malic acid.", example = "4.5")
    @NonNull BigDecimal acidityMin,
    @Schema(description = "Maximum titratable acidity coordinate in g/L as malic acid.", example = "7.5")
    @NonNull BigDecimal acidityMax
) implements ProfileTargetRequest {

  public CustomRectangleProfileTargetRequest {
    requireNonNull(kind, "target kind cannot be null");
    requireNonNull(sugarMin, "target sugar minimum cannot be null");
    requireNonNull(sugarMax, "target sugar maximum cannot be null");
    requireNonNull(acidityMin, "target acidity minimum cannot be null");
    requireNonNull(acidityMax, "target acidity maximum cannot be null");
  }
}
