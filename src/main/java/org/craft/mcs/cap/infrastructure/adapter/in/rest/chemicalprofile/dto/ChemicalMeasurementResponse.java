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
        @JsonSubTypes.Type(value = RangeChemicalMeasurementResponse.class, name = "RANGE"),
        @JsonSubTypes.Type(value = StatisticalChemicalMeasurementResponse.class, name = "STATISTICAL")
    }
)
@Schema(
    description = "Chemical measurement response. Uses EXACT, RANGE or STATISTICAL shape.",
    discriminatorProperty = "kind",
    oneOf = {
        ExactChemicalMeasurementResponse.class,
        RangeChemicalMeasurementResponse.class,
        StatisticalChemicalMeasurementResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "EXACT", schema = ExactChemicalMeasurementResponse.class),
        @DiscriminatorMapping(value = "RANGE", schema = RangeChemicalMeasurementResponse.class),
        @DiscriminatorMapping(value = "STATISTICAL", schema = StatisticalChemicalMeasurementResponse.class)
    }
)
public sealed interface ChemicalMeasurementResponse
    permits ExactChemicalMeasurementResponse, RangeChemicalMeasurementResponse, StatisticalChemicalMeasurementResponse {

  @NonNull
  @Schema(description = "Measurement shape discriminator.", example = "EXACT", requiredMode = Schema.RequiredMode.REQUIRED)
  String kind();
}
