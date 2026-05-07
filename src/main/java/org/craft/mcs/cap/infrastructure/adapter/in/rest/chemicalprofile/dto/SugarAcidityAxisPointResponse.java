package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "valueKind",
    visible = true
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = ExactSugarAcidityAxisPointResponse.class, name = "EXACT"),
        @JsonSubTypes.Type(value = RangeSugarAcidityAxisPointResponse.class, name = "RANGE_MIDPOINT"),
        @JsonSubTypes.Type(value = StatisticalSugarAcidityAxisPointResponse.class, name = "STATISTICAL_MEAN"),
        @JsonSubTypes.Type(value = StatisticalSugarAcidityAxisPointResponse.class, name = "STATISTICAL_MEDIAN")
    }
)
public sealed interface SugarAcidityAxisPointResponse
    permits ExactSugarAcidityAxisPointResponse,
    RangeSugarAcidityAxisPointResponse,
    StatisticalSugarAcidityAxisPointResponse {

  @NonNull
  BigDecimal value();

  @NonNull
  String valueKind();
}
