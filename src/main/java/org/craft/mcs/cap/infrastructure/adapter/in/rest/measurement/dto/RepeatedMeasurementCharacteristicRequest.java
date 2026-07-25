package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementDerivationMethod;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;


@Schema(
    description = """
        Repeated POINT observations retained separately with one derived working RESULT.
        INTERVAL values are not accepted as observations.
        """
)
public record RepeatedMeasurementCharacteristicRequest(
    @Schema(
        description = "Polymorphic input discriminator; must be REPEATED.",
        implementation = MeasurementInputKind.class,
        allowableValues = "REPEATED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementInputKind inputKind,
    @Schema(
        description = "Characteristic measured by every observation in the series.",
        implementation = MeasurementCharacteristic.class,
        example = "SPECIFIC_GRAVITY",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementCharacteristic characteristic,
    @ArraySchema(
        minItems = 2,
        arraySchema = @Schema(
            description = "Independent point observations retained as immutable evidence.",
            requiredMode = Schema.RequiredMode.REQUIRED
        ),
        schema = @Schema(implementation = MeasurementObservationRequest.class)
    )
    @NonNull List<@NonNull MeasurementObservationRequest> observations,
    @Schema(
        description = "Transparent method used to derive the single working RESULT.",
        implementation = MeasurementDerivationMethod.class,
        example = "ARITHMETIC_MEAN",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull MeasurementDerivationMethod derivationMethod
) implements MeasurementCharacteristicRequest {

  public RepeatedMeasurementCharacteristicRequest {
    if (inputKind != MeasurementInputKind.REPEATED) {
      throw new ApplicationValidationException("repeated measurement input kind expected");
    }
  }
}
