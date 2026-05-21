package org.craft.mcs.cap.shared.infrastructure.rest.profiletarget.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Optional tannin target range in mg/L TAE.")
public record ProfileTargetTanninRangeRequest(
    @Schema(description = "Minimum tannin target in mg/L TAE.", example = "800")
    @NonNull BigDecimal min,
    @Schema(description = "Maximum tannin target in mg/L TAE.", example = "1800")
    @NonNull BigDecimal max
) {

  public ProfileTargetTanninRangeRequest {
    requireNonNull(min, "tannin target minimum cannot be null");
    requireNonNull(max, "tannin target maximum cannot be null");
  }
}
