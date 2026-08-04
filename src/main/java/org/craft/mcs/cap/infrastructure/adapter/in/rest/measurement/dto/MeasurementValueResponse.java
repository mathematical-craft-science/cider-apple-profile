package org.craft.mcs.cap.infrastructure.adapter.in.rest.measurement.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import org.craft.mcs.cap.core.measurement.domain.model.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


/** Canonical value returned for immutable measurement evidence. */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kind",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PointMeasurementValueResponse.class, name = "POINT"),
    @JsonSubTypes.Type(value = IntervalMeasurementValueResponse.class, name = "INTERVAL")
})
@Schema(
    description = """
        Canonical POINT or INTERVAL retained by immutable measurement evidence. Every physical quantity includes
        its canonical unit label.
        """,
    discriminatorProperty = "kind",
    oneOf = {
        PointMeasurementValueResponse.class,
        IntervalMeasurementValueResponse.class
    },
    discriminatorMapping = {
        @DiscriminatorMapping(value = "POINT", schema = PointMeasurementValueResponse.class),
        @DiscriminatorMapping(value = "INTERVAL", schema = IntervalMeasurementValueResponse.class)
    }
)
public sealed interface MeasurementValueResponse
    permits PointMeasurementValueResponse, IntervalMeasurementValueResponse {

  @Schema(
      description = "Discriminator identifying the retained value shape.",
      requiredMode = Schema.RequiredMode.REQUIRED
  )
  @NonNull MeasurementValueKind kind();
}
