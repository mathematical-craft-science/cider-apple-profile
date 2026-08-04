package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Half-open [min, max) coordinate range of a rectangular SG/TA zone.")
public record SugarAcidityGraphRangeResponse(
    @Schema(
        description = "Inclusive range minimum.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse min,
    @Schema(
        description = "Exclusive range maximum.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse max
) {

  public SugarAcidityGraphRangeResponse(
      @NonNull BigDecimal min,
      @NonNull BigDecimal max,
      @NonNull String unit
  ) {
    this(new DecimalQuantityResponse(min, unit), new DecimalQuantityResponse(max, unit));
  }
}
