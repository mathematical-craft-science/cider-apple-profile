package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Statistical chemical measurement response.")
public record StatisticalChemicalMeasurementResponse(
    @Schema(description = "Measurement shape.", example = "STATISTICAL")
    @NonNull String kind,
    @Schema(description = "Canonical measurement unit.", example = "g/L as malic acid")
    @NonNull String unit,
    @Schema(description = "Mean canonical value.", example = "5.2")
    @NonNull BigDecimal mean,
    @Schema(description = "Median canonical value, when available.", example = "5.1")
    @Nullable BigDecimal median,
    @Schema(description = "Standard deviation in canonical units, when available.", example = "0.2")
    @Nullable BigDecimal standardDeviation,
    @Schema(description = "Number of source measurements.", example = "3")
    @Nullable Integer sampleSize
) implements ChemicalMeasurementResponse {

  public StatisticalChemicalMeasurementResponse {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");
    requireNonNull(mean, "statistical mean value cannot be null");
  }
}
