package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.dto;

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
        @JsonSubTypes.Type(value = PredefinedZoneFeedstockBatchBlendProfileTargetRequest.class, name = "PREDEFINED_ZONE"),
        @JsonSubTypes.Type(value = CustomRectangleFeedstockBatchBlendProfileTargetRequest.class, name = "CUSTOM_RECTANGLE")
    }
)
public sealed interface FeedstockBatchBlendProfileTargetRequest
    permits PredefinedZoneFeedstockBatchBlendProfileTargetRequest, CustomRectangleFeedstockBatchBlendProfileTargetRequest {

  @NonNull
  String kind();
}
