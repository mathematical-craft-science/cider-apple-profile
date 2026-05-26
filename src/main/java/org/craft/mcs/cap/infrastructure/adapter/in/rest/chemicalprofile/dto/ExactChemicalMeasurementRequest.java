package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Exact chemical measurement request.")
public record ExactChemicalMeasurementRequest(
    @Schema(description = "Measurement shape.", example = "EXACT", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull String kind,
    @Schema(description = "External measurement unit.", example = "SG", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable String unit,
    @Schema(description = "Measured value.", example = "1.052", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal value
) implements ChemicalMeasurementRequest {

  public ExactChemicalMeasurementRequest {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(value, "exact measurement value cannot be null");
  }

  public ExactChemicalMeasurementRequest(
      @NonNull String kind,
      @NonNull BigDecimal value
  ) {
    this(kind, null, value);
  }
}
