package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementValueKind;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


@Schema(
    description = """
        Source-asserted lower and upper bounds. An interval is not a statistical sample and does not imply a mean.
        """
)
public record IntervalMeasurementValueRequest(
    @Schema(
        description = "Polymorphic value discriminator; must be INTERVAL.",
        allowableValues = "INTERVAL",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind kind,
    @Schema(
        description = "Inclusive lower bound expressed in the accompanying input unit.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal lower,
    @Schema(
        description = "Inclusive upper bound. It must be greater than or equal to lower.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal upper,
    @Schema(
        description = """
            Unit label shared by both bounds. The REST ACL validates compatibility and converts both values to the
            canonical domain unit.
            """,
        minLength = 1,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String unit
) implements MeasurementValueRequest {

  public IntervalMeasurementValueRequest {
    if (kind != MeasurementValueKind.INTERVAL) {
      throw new ApplicationValidationException("interval measurement value kind expected");
    }
  }
}
