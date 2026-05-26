package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Closed numeric range on a sugar-acidity graph axis.")
public record SugarAcidityGraphRangeResponse(
    @Schema(description = "Range minimum.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse min,
    @Schema(description = "Range maximum.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse max
) {

  public SugarAcidityGraphRangeResponse(
      @NonNull BigDecimal min,
      @NonNull BigDecimal max,
      @NonNull String unit
  ) {
    this(new DecimalQuantityResponse(min, unit), new DecimalQuantityResponse(max, unit));
  }
}
