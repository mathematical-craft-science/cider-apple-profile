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
        @JsonSubTypes.Type(value = RangeChemicalMeasurementRequest.class, name = "RANGE"),
        @JsonSubTypes.Type(value = StatisticalChemicalMeasurementRequest.class, name = "STATISTICAL")
    }
)
@Schema(
    description = "Chemical measurement request. Use EXACT, RANGE or STATISTICAL shape.",
    discriminatorProperty = "kind",
    oneOf = {
        ExactChemicalMeasurementRequest.class,
        RangeChemicalMeasurementRequest.class,
        StatisticalChemicalMeasurementRequest.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "EXACT", schema = ExactChemicalMeasurementRequest.class),
        @DiscriminatorMapping(value = "RANGE", schema = RangeChemicalMeasurementRequest.class),
        @DiscriminatorMapping(value = "STATISTICAL", schema = StatisticalChemicalMeasurementRequest.class)
    }
)
public sealed interface ChemicalMeasurementRequest
    permits ExactChemicalMeasurementRequest, RangeChemicalMeasurementRequest, StatisticalChemicalMeasurementRequest {

  @NonNull
  @Schema(description = "Measurement shape discriminator.", example = "EXACT")
  String kind();
}
