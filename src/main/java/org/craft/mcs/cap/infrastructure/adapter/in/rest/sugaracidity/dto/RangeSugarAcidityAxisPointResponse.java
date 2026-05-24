package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Range-based graph-axis point represented by a midpoint value.")
public record RangeSugarAcidityAxisPointResponse(
    @Schema(description = "Midpoint axis value used as graph coordinate.", example = "1.054", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal value,
    @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "RANGE_MIDPOINT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueKind valueKind,
    @Schema(description = "Minimum measured value.", example = "1.048", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal min,
    @Schema(description = "Maximum measured value.", example = "1.060", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal max
) implements SugarAcidityAxisPointResponse {

  public RangeSugarAcidityAxisPointResponse(
      @NonNull BigDecimal value,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    this(value, MeasurementValueKind.RANGE_MIDPOINT, min, max);
  }

  public RangeSugarAcidityAxisPointResponse {
    requireNonNull(value, "range axis point value cannot be null");
    requireNonNull(valueKind, "range axis point value kind cannot be null");
    requireNonNull(min, "range axis point min cannot be null");
    requireNonNull(max, "range axis point max cannot be null");

    if (valueKind != MeasurementValueKind.RANGE_MIDPOINT) {
      throw new IllegalArgumentException("range axis point value kind expected");
    }
  }
}
