package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Statistical chemical measurement response.")
public record StatisticalChemicalMeasurementResponse(
    @Schema(description = "Measurement shape.", example = "STATISTICAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String kind,
    @Schema(description = "Mean canonical value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse mean,
    @Schema(description = "Median canonical value, when available.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable DecimalQuantityResponse median,
    @Schema(description = "Standard deviation in canonical units, when available.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable DecimalQuantityResponse standardDeviation,
    @Schema(description = "Number of source measurements.", example = "3", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Nullable Integer sampleSize
) implements ChemicalMeasurementResponse {

  public StatisticalChemicalMeasurementResponse {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(mean, "statistical mean value cannot be null");
  }
}
