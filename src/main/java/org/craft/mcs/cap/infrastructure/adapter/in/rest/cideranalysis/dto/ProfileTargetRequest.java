package org.craft.mcs.cap.infrastructure.adapter.in.rest.cideranalysis.dto;

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
        @JsonSubTypes.Type(value = PredefinedZoneProfileTargetRequest.class, name = "PREDEFINED_ZONE"),
        @JsonSubTypes.Type(value = CustomRectangleProfileTargetRequest.class, name = "CUSTOM_RECTANGLE")
    }
)
public sealed interface ProfileTargetRequest
    permits PredefinedZoneProfileTargetRequest, CustomRectangleProfileTargetRequest {

  @NonNull
  String kind();
}
