package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import org.jspecify.annotations.NonNull;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "shape",
    visible = true
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = RectangleSugarAcidityGraphZoneResponse.class, name = "RECTANGLE"),
        @JsonSubTypes.Type(value = EllipseSugarAcidityGraphZoneResponse.class, name = "ELLIPSE")
    }
)
public sealed interface SugarAcidityGraphZoneResponse
    permits RectangleSugarAcidityGraphZoneResponse, EllipseSugarAcidityGraphZoneResponse {

  @NonNull
  String zone();

  @NonNull
  String shape();
}
