package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Point expressed in the characteristic's canonical unit.")
public record PointMeasurementValueResponse(
    @Schema(
        description = "Polymorphic value discriminator; always POINT.",
        type = "string",
        allowableValues = "POINT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind kind,
    @Schema(
        description = "Canonical numeric value and unit label.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse value
) implements MeasurementValueResponse {

  public PointMeasurementValueResponse {
    if (kind != MeasurementValueKind.POINT) {
      throw new ApplicationValidationException("point measurement value kind expected");
    }
  }
}
