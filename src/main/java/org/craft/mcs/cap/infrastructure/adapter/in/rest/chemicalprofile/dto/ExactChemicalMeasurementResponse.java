package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Exact chemical measurement response.")
public record ExactChemicalMeasurementResponse(
    @Schema(
        description = "Measurement shape.",
        type = "string",
        allowableValues = "EXACT",
        example = "EXACT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String kind,
    @Schema(description = "Canonical measured value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse value
) implements ChemicalMeasurementResponse {

  public ExactChemicalMeasurementResponse {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(value, "exact measurement value cannot be null");
  }
}
