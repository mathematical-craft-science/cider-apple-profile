package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Exact chemical measurement response.")
public record ExactChemicalMeasurementResponse(
    @Schema(description = "Measurement shape.", example = "EXACT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String kind,
    @Schema(description = "Canonical measurement unit.", example = "SG", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull String unit,
    @Schema(description = "Canonical measured value.", example = "1.052", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull BigDecimal value
) implements ChemicalMeasurementResponse {

  public ExactChemicalMeasurementResponse {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(unit, "measurement unit cannot be null");
    requireNonNull(value, "exact measurement value cannot be null");
  }
}
