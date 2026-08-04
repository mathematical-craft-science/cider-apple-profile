package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.kernel.measurement.MeasurementCharacteristic;
import org.jspecify.annotations.NonNull;


/**
 * Mutually exclusive direct or repeated-observation input for one characteristic.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "inputKind",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DirectMeasurementCharacteristicRequest.class, name = "DIRECT"),
    @JsonSubTypes.Type(value = RepeatedMeasurementCharacteristicRequest.class, name = "REPEATED")
})
@Schema(
    description = """
        One characteristic supplied either as a direct POINT/INTERVAL result or as repeated POINT observations.
        A characteristic can occur only once in a measurement-set revision.
        """,
    discriminatorProperty = "inputKind",
    discriminatorMapping = {
        @DiscriminatorMapping(value = "DIRECT", schema = DirectMeasurementCharacteristicRequest.class),
        @DiscriminatorMapping(value = "REPEATED", schema = RepeatedMeasurementCharacteristicRequest.class)
    },
    oneOf = {
        DirectMeasurementCharacteristicRequest.class,
        RepeatedMeasurementCharacteristicRequest.class
    }
)
public sealed interface MeasurementCharacteristicRequest
    permits DirectMeasurementCharacteristicRequest, RepeatedMeasurementCharacteristicRequest {

  @Schema(
      description = "Discriminator selecting direct input or repeated observations.",
      implementation = MeasurementInputKind.class,
      example = "DIRECT",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NonNull MeasurementInputKind inputKind();

  @Schema(
      description = "Material or process characteristic represented by this input.",
      implementation = MeasurementCharacteristic.class,
      example = "SPECIFIC_GRAVITY",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NonNull MeasurementCharacteristic characteristic();
}
