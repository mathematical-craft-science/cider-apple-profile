package org.craft.mcs.cap.infrastructure.adapter.in.importer.appleprofile.dto;

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
        @JsonSubTypes.Type(value = ExactMeasurementSeedDto.class, name = "EXACT"),
        @JsonSubTypes.Type(value = RangeMeasurementSeedDto.class, name = "RANGE"),
        @JsonSubTypes.Type(value = StatisticalMeasurementSeedDto.class, name = "STATISTICAL")
    }
)
public sealed interface MeasurementSeedDto
    permits ExactMeasurementSeedDto, RangeMeasurementSeedDto, StatisticalMeasurementSeedDto {

  @NonNull
  MeasurementSeedKind kind();

  @NonNull
  MeasurementSeedUnit unit();
}
