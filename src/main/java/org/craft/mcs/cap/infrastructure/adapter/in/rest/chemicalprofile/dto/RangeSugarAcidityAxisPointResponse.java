package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.domain.classification.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Range-based graph-axis point represented by a midpoint value.")
public record RangeSugarAcidityAxisPointResponse(
    @Schema(description = "Midpoint axis value used as graph coordinate.", example = "1.054") @NonNull BigDecimal value,
    @Schema(description = "Minimum measured value.", example = "1.048") @NonNull BigDecimal min,
    @Schema(description = "Maximum measured value.", example = "1.060") @NonNull BigDecimal max
) implements SugarAcidityAxisPointResponse {

  @Override
  @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "RANGE_MIDPOINT")
  public @NonNull MeasurementValueKind valueKind() {
    return MeasurementValueKind.RANGE_MIDPOINT;
  }
}
