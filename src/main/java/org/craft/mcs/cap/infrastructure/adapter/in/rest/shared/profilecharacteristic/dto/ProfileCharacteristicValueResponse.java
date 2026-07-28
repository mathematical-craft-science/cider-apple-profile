package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "valueKind",
    visible = true
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = PointProfileCharacteristicValueResponse.class, name = "POINT"),
        @JsonSubTypes.Type(value = IntervalProfileCharacteristicValueResponse.class, name = "INTERVAL_MIDPOINT"),
        @JsonSubTypes.Type(value = CalculatedProfileCharacteristicValueResponse.class, name = "CALCULATED")
    }
)
@Schema(
    description = "Profile characteristic value with its projection kind.",
    discriminatorProperty = "valueKind",
    oneOf = {
        PointProfileCharacteristicValueResponse.class,
        IntervalProfileCharacteristicValueResponse.class,
        CalculatedProfileCharacteristicValueResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "POINT", schema = PointProfileCharacteristicValueResponse.class),
        @DiscriminatorMapping(
            value = "INTERVAL_MIDPOINT",
            schema = IntervalProfileCharacteristicValueResponse.class
        ),
        @DiscriminatorMapping(value = "CALCULATED", schema = CalculatedProfileCharacteristicValueResponse.class)
    }
)
public sealed interface ProfileCharacteristicValueResponse
    permits PointProfileCharacteristicValueResponse,
    IntervalProfileCharacteristicValueResponse,
    CalculatedProfileCharacteristicValueResponse {

  @NonNull
  @Schema(description = "Representative characteristic value.", requiredMode = Schema.RequiredMode.REQUIRED)
  DecimalQuantityResponse value();

  @NonNull
  @Schema(
      description = "Characteristic projection kind.",
      implementation = AxisValueKind.class,
      example = "POINT",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  AxisValueKind valueKind();
}
