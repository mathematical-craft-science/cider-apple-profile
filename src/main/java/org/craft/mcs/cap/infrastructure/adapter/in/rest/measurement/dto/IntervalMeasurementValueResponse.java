package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementValueKind;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;


@Schema(description = "Interval expressed in the characteristic's canonical unit; no midpoint is persisted.")
public record IntervalMeasurementValueResponse(
    @Schema(
        description = "Polymorphic value discriminator; always INTERVAL.",
        allowableValues = "INTERVAL",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind kind,
    @Schema(
        description = "Inclusive lower bound and canonical unit label.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse lower,
    @Schema(
        description = "Inclusive upper bound and the same canonical unit label.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse upper
) implements MeasurementValueResponse {

  public IntervalMeasurementValueResponse {
    if (kind != MeasurementValueKind.INTERVAL) {
      throw new ApplicationValidationException("interval measurement value kind expected");
    }
  }
}
