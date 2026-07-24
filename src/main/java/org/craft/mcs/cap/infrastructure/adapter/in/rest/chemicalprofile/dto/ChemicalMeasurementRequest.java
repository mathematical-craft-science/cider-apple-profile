package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kind",
    visible = true
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = ExactChemicalMeasurementRequest.class, name = "EXACT"),
        @JsonSubTypes.Type(value = RangeChemicalMeasurementRequest.class, name = "RANGE")
    }
)
@Schema(
    description = "Legacy chemical profile value. Use EXACT or RANGE shape.",
    discriminatorProperty = "kind",
    oneOf = {
        ExactChemicalMeasurementRequest.class,
        RangeChemicalMeasurementRequest.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "EXACT", schema = ExactChemicalMeasurementRequest.class),
        @DiscriminatorMapping(value = "RANGE", schema = RangeChemicalMeasurementRequest.class)
    }
)
public sealed interface ChemicalMeasurementRequest
    permits ExactChemicalMeasurementRequest, RangeChemicalMeasurementRequest {

  @NonNull
  @Schema(
      description = "Measurement shape discriminator.",
      example = "EXACT",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  String kind();
}
