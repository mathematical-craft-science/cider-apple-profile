package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "valueKind",
    visible = true
)
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = PointSugarAcidityAxisPointResponse.class, name = "POINT"),
        @JsonSubTypes.Type(value = IntervalSugarAcidityAxisPointResponse.class, name = "INTERVAL_MIDPOINT"),
        @JsonSubTypes.Type(value = CalculatedSugarAcidityAxisPointResponse.class, name = "CALCULATED")
    }
)
@Schema(
    description = "Graph-axis point value with source value kind.",
    discriminatorProperty = "valueKind",
    oneOf = {
        PointSugarAcidityAxisPointResponse.class,
        IntervalSugarAcidityAxisPointResponse.class,
        CalculatedSugarAcidityAxisPointResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "POINT", schema = PointSugarAcidityAxisPointResponse.class),
        @DiscriminatorMapping(
            value = "INTERVAL_MIDPOINT",
            schema = IntervalSugarAcidityAxisPointResponse.class
        ),
        @DiscriminatorMapping(value = "CALCULATED", schema = CalculatedSugarAcidityAxisPointResponse.class)
    }
)
public sealed interface SugarAcidityAxisPointResponse
    permits PointSugarAcidityAxisPointResponse,
    IntervalSugarAcidityAxisPointResponse,
    CalculatedSugarAcidityAxisPointResponse {

  @NonNull
  @Schema(description = "Coordinate value for the graph axis.", requiredMode = Schema.RequiredMode.REQUIRED)
  DecimalQuantityResponse value();

  @NonNull
  @Schema(
      description = "Axis point value discriminator.",
      implementation = AxisValueKind.class,
      example = "POINT",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  AxisValueKind valueKind();
}
