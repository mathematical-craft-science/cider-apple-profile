package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain.SugarAcidityGraphZoneShape;
import org.jspecify.annotations.NonNull;


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
@Schema(
    description = "Sugar-acidity graph zone geometry.",
    discriminatorProperty = "shape",
    oneOf = {
        RectangleSugarAcidityGraphZoneResponse.class,
        EllipseSugarAcidityGraphZoneResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "RECTANGLE", schema = RectangleSugarAcidityGraphZoneResponse.class),
        @DiscriminatorMapping(value = "ELLIPSE", schema = EllipseSugarAcidityGraphZoneResponse.class)
    }
)
public sealed interface SugarAcidityGraphZoneResponse
    permits RectangleSugarAcidityGraphZoneResponse, EllipseSugarAcidityGraphZoneResponse {

  @NonNull
  @Schema(description = "Zone code.", implementation = SugarAcidityZone.class, example = "PERFECT_APPLES", requiredMode = Schema.RequiredMode.REQUIRED)
  SugarAcidityZone zone();

  @NonNull
  @Schema(description = "Zone shape discriminator.", implementation = SugarAcidityGraphZoneShape.class, example = "RECTANGLE", requiredMode = Schema.RequiredMode.REQUIRED)
  SugarAcidityGraphZoneShape shape();
}
