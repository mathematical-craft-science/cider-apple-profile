package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Range chemical measurement response.")
public record RangeChemicalMeasurementResponse(
    @Schema(
        description = "Measurement shape.",
        type = "string",
        allowableValues = "RANGE",
        example = "RANGE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String kind,
    @Schema(description = "Minimum canonical value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse min,
    @Schema(description = "Maximum canonical value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse max
) implements ChemicalMeasurementResponse {

  public RangeChemicalMeasurementResponse {
    requireNonNull(kind, "measurement kind cannot be null");
    requireNonNull(min, "range minimum value cannot be null");
    requireNonNull(max, "range maximum value cannot be null");
  }
}
