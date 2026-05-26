package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Statistical chemical measurement request.")
public record StatisticalChemicalMeasurementRequest(
    @Schema(description = "Measurement shape.", example = "STATISTICAL", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String kind,
    @Schema(description = "External measurement unit.", example = "g/L as malic acid", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable String unit,
    @Schema(description = "Mean measured value.", example = "5.2", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal mean,
    @Schema(description = "Median measured value, when available.", example = "5.1", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable BigDecimal median,
    @Schema(description = "Standard deviation, when available.", example = "0.2", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable BigDecimal standardDeviation,
    @Schema(description = "Number of measurements in the statistical sample.", example = "3", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable Integer sampleSize
) implements ChemicalMeasurementRequest {

  public StatisticalChemicalMeasurementRequest {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(mean, "statistical mean value cannot be null");
  }

  public StatisticalChemicalMeasurementRequest(
      @NonNull String kind,
      @NonNull BigDecimal mean,
      @Nullable BigDecimal median,
      @Nullable BigDecimal standardDeviation,
      @Nullable Integer sampleSize
  ) {
    this(kind, null, mean, median, standardDeviation, sampleSize);
  }
}
