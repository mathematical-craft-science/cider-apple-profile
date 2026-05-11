package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


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
        @JsonSubTypes.Type(value = StatisticalSugarAcidityAxisPointResponse.class, name = "STATISTICAL_MEDIAN"),
        @JsonSubTypes.Type(value = CalculatedSugarAcidityAxisPointResponse.class, name = "CALCULATED")
    }
)
@Schema(
    description = "Graph-axis point value with source value kind.",
    discriminatorProperty = "valueKind",
    oneOf = {
        ExactSugarAcidityAxisPointResponse.class,
        RangeSugarAcidityAxisPointResponse.class,
        StatisticalSugarAcidityAxisPointResponse.class,
        CalculatedSugarAcidityAxisPointResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "EXACT", schema = ExactSugarAcidityAxisPointResponse.class),
        @DiscriminatorMapping(value = "RANGE_MIDPOINT", schema = RangeSugarAcidityAxisPointResponse.class),
        @DiscriminatorMapping(value = "STATISTICAL_MEAN", schema = StatisticalSugarAcidityAxisPointResponse.class),
        @DiscriminatorMapping(value = "STATISTICAL_MEDIAN", schema = StatisticalSugarAcidityAxisPointResponse.class),
        @DiscriminatorMapping(value = "CALCULATED", schema = CalculatedSugarAcidityAxisPointResponse.class)
    }
)
public sealed interface SugarAcidityAxisPointResponse
    permits ExactSugarAcidityAxisPointResponse,
    RangeSugarAcidityAxisPointResponse,
    StatisticalSugarAcidityAxisPointResponse,
    CalculatedSugarAcidityAxisPointResponse {

  @NonNull
  @Schema(description = "Coordinate value for the graph axis.", example = "1.052")
  BigDecimal value();

  @NonNull
  @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "EXACT")
  MeasurementValueKind valueKind();
}
