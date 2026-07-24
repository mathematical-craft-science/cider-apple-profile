package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementValueKind;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


@Schema(
    description = "One reported point quantity converted to the characteristic's canonical value at ingress."
)
public record PointMeasurementValueRequest(
    @Schema(
        description = "Polymorphic value discriminator; must be POINT.",
        allowableValues = "POINT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueKind kind,
    @Schema(
        description = "Numeric point value expressed in the accompanying input unit.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull BigDecimal value,
    @Schema(
        description = """
            Unit label accepted for the selected characteristic. The REST ACL validates compatibility and converts
            the value to the canonical domain unit.
            """,
        minLength = 1,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull String unit
) implements MeasurementValueRequest {

  public PointMeasurementValueRequest {
    if (kind != MeasurementValueKind.POINT) {
      throw new ApplicationValidationException("point measurement value kind expected");
    }
  }
}
