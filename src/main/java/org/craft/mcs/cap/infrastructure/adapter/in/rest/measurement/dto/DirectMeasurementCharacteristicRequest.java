package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementCharacteristic;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


@Schema(
    description = "One direct POINT or INTERVAL result converted to its canonical domain value at ingress."
)
public record DirectMeasurementCharacteristicRequest(
    @Schema(
        description = "Polymorphic input discriminator; must be DIRECT.",
        allowableValues = "DIRECT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementInputKind inputKind,
    @Schema(
        description = "Characteristic measured by this result.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @Schema(
        description = "Point or source-asserted interval accepted with an explicit input unit.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementValueRequest value,
    @Schema(
        description = "Time when the material or process characteristic was measured.",
        format = "date-time",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull Instant measuredAt,
    @Schema(
        description = "Source, method, reference, and confidence attached to this result.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementProvenanceRequest provenance
) implements MeasurementCharacteristicRequest {

  public DirectMeasurementCharacteristicRequest {
    if (inputKind != MeasurementInputKind.DIRECT) {
      throw new ApplicationValidationException("direct measurement input kind expected");
    }
  }
}
