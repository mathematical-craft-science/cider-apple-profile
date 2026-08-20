package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;


/** Strict inbound value union; an interval is not a statistical sample. */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kind",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PointMeasurementValueRequest.class, name = "POINT"),
    @JsonSubTypes.Type(value = IntervalMeasurementValueRequest.class, name = "INTERVAL")
})
@Schema(
    description = """
        Inbound POINT or source-asserted INTERVAL value. The explicit input unit is converted by the REST ACL
        before the value enters the application core.
        """,
    discriminatorProperty = "kind",
    discriminatorMapping = {
        @DiscriminatorMapping(value = "POINT", schema = PointMeasurementValueRequest.class),
        @DiscriminatorMapping(value = "INTERVAL", schema = IntervalMeasurementValueRequest.class)
    },
    oneOf = {
        PointMeasurementValueRequest.class,
        IntervalMeasurementValueRequest.class
    }
)
public sealed interface MeasurementValueRequest
    permits PointMeasurementValueRequest, IntervalMeasurementValueRequest {

  @Schema(
      description = "Discriminator selecting a point or interval value.",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NonNull MeasurementValueKind kind();
}
