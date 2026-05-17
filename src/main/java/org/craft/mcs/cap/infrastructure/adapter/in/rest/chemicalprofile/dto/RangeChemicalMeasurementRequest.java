package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Range chemical measurement request.")
public record RangeChemicalMeasurementRequest(
    @Schema(description = "Measurement shape.", example = "RANGE") @NonNull String kind,
    @Schema(description = "External measurement unit.", example = "g/L as malic acid") @Nullable String unit,
    @Schema(description = "Minimum measured value.", example = "1.048") @NonNull BigDecimal min,
    @Schema(description = "Maximum measured value.", example = "1.060") @NonNull BigDecimal max
) implements ChemicalMeasurementRequest {

  public RangeChemicalMeasurementRequest {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(min, "range minimum value cannot be null");
    requireNonNull(max, "range maximum value cannot be null");
  }

  public RangeChemicalMeasurementRequest(
      @NonNull String kind,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max
  ) {
    this(kind, null, min, max);
  }
}
