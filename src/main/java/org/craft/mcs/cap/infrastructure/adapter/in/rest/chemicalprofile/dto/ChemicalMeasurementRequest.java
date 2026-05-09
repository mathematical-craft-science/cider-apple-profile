package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
public sealed interface ChemicalMeasurementRequest
    permits ExactChemicalMeasurementRequest, RangeChemicalMeasurementRequest, StatisticalChemicalMeasurementRequest {

  @NonNull
  String kind();
}
