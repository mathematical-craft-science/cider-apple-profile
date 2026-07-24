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
        @JsonSubTypes.Type(value = ExactChemicalMeasurementResponse.class, name = "EXACT"),
        @JsonSubTypes.Type(value = RangeChemicalMeasurementResponse.class, name = "RANGE")
    }
)
@Schema(
    description = "Legacy chemical profile value. Uses EXACT or RANGE shape.",
    discriminatorProperty = "kind",
    oneOf = {
        ExactChemicalMeasurementResponse.class,
        RangeChemicalMeasurementResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "EXACT", schema = ExactChemicalMeasurementResponse.class),
        @DiscriminatorMapping(value = "RANGE", schema = RangeChemicalMeasurementResponse.class)
    }
)
public sealed interface ChemicalMeasurementResponse
    permits ExactChemicalMeasurementResponse, RangeChemicalMeasurementResponse {

  @NonNull
  @Schema(
      description = "Measurement shape discriminator.",
      example = "EXACT",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  String kind();
}
