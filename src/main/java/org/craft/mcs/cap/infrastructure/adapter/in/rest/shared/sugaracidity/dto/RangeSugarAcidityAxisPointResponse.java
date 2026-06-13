package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Range-based graph-axis point represented by a midpoint value.")
public record RangeSugarAcidityAxisPointResponse(
    @Schema(description = "Midpoint axis value used as graph coordinate.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse value,
    @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "RANGE_MIDPOINT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueKind valueKind,
    @Schema(description = "Minimum measured value.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse min,
    @Schema(description = "Maximum measured value.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse max
) implements SugarAcidityAxisPointResponse {

  public RangeSugarAcidityAxisPointResponse(
      @NonNull BigDecimal value,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max,
      @NonNull String unit
  ) {
    this(
        new DecimalQuantityResponse(value, unit),
        MeasurementValueKind.RANGE_MIDPOINT,
        new DecimalQuantityResponse(min, unit),
        new DecimalQuantityResponse(max, unit)
    );
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
