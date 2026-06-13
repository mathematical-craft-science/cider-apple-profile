package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto;

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
        @JsonSubTypes.Type(value = PredefinedZoneProfileTargetRequest.class, name = "PREDEFINED_ZONE"),
        @JsonSubTypes.Type(value = CustomRectangleProfileTargetRequest.class, name = "CUSTOM_RECTANGLE")
    }
)
@Schema(
    description = "Profile target request. Use PREDEFINED_ZONE or CUSTOM_RECTANGLE shape.",
    discriminatorProperty = "kind",
    oneOf = {PredefinedZoneProfileTargetRequest.class, CustomRectangleProfileTargetRequest.class},
    discriminatorMapping = {
        @DiscriminatorMapping(value = "PREDEFINED_ZONE", schema = PredefinedZoneProfileTargetRequest.class),
        @DiscriminatorMapping(value = "CUSTOM_RECTANGLE", schema = CustomRectangleProfileTargetRequest.class)
    }
)
public sealed interface ProfileTargetRequest
    permits PredefinedZoneProfileTargetRequest, CustomRectangleProfileTargetRequest {

  @NonNull
  @Schema(description = "Target shape discriminator.", example = "PREDEFINED_ZONE")
  String kind();
}
