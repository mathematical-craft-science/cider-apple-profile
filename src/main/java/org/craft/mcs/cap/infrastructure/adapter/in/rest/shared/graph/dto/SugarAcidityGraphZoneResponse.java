package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.graph.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGeometryShape;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.jspecify.annotations.NonNull;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "shape",
    visible = true
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(
            value = RectangleSugarAcidityGraphZoneResponse.class,
            name = "RECTANGLE"
        ),
        @JsonSubTypes.Type(
            value = EllipseSugarAcidityGraphZoneResponse.class,
            name = "ELLIPSE"
        )
    }
)
@Schema(
    description = "Jolicoeur sugar-acidity graph zone geometry expressed in SG and TA coordinates.",
    discriminatorProperty = "shape",
    oneOf = {
        RectangleSugarAcidityGraphZoneResponse.class,
        EllipseSugarAcidityGraphZoneResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(
            value = "RECTANGLE",
            schema = RectangleSugarAcidityGraphZoneResponse.class
        ),
        @DiscriminatorMapping(
            value = "ELLIPSE",
            schema = EllipseSugarAcidityGraphZoneResponse.class
        )
    }
)
public sealed interface SugarAcidityGraphZoneResponse
    permits RectangleSugarAcidityGraphZoneResponse,
    EllipseSugarAcidityGraphZoneResponse {

  @NonNull
  @Schema(
      description = "Jolicoeur zone code.",
      implementation = JolicoeurSugarAcidityZone.class,
      example = "PERFECT_APPLES",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  JolicoeurSugarAcidityZone zone();

  @NonNull
  @Schema(
      description = "Zone shape discriminator.",
      implementation = SugarAcidityGeometryShape.class,
      example = "RECTANGLE",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  SugarAcidityGeometryShape shape();
}
