package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Range chemical measurement response.")
public record RangeChemicalMeasurementResponse(
    @Schema(description = "Measurement shape.", example = "RANGE")
    @NonNull String kind,
    @Schema(description = "Canonical measurement unit.", example = "g/L as malic acid")
    @NonNull String unit,
    @Schema(description = "Minimum canonical value.", example = "4.8")
    @NonNull BigDecimal min,
    @Schema(description = "Maximum canonical value.", example = "6.2")
    @NonNull BigDecimal max
) implements ChemicalMeasurementResponse {

  public RangeChemicalMeasurementResponse {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");
    requireNonNull(min, "range minimum value cannot be null");
    requireNonNull(max, "range maximum value cannot be null");
  }
}
