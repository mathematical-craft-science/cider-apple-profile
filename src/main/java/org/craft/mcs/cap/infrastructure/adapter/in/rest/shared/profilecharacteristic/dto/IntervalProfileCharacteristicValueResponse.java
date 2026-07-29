package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Interval profile characteristic represented by its computed midpoint.")
public record IntervalProfileCharacteristicValueResponse(
    @Schema(
        description = "Midpoint used as the representative characteristic value.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "Characteristic projection kind.",
        type = "string",
        allowableValues = "INTERVAL_MIDPOINT",
        example = "INTERVAL_MIDPOINT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileCharacteristicValueKind valueKind,
    @Schema(description = "Minimum measured value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse min,
    @Schema(description = "Maximum measured value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse max
) implements ProfileCharacteristicValueResponse {

  public IntervalProfileCharacteristicValueResponse(
      @NonNull BigDecimal value,
      @NonNull BigDecimal min,
      @NonNull BigDecimal max,
      @NonNull String unit
  ) {
    this(
        new DecimalQuantityResponse(value, unit),
        ProfileCharacteristicValueKind.INTERVAL_MIDPOINT,
        new DecimalQuantityResponse(min, unit),
        new DecimalQuantityResponse(max, unit)
    );
  }

  public IntervalProfileCharacteristicValueResponse {
    requireNonNull(value, "interval characteristic representative value cannot be null");
    requireNonNull(valueKind, "interval characteristic value kind cannot be null");
    requireNonNull(min, "interval characteristic minimum cannot be null");
    requireNonNull(max, "interval characteristic maximum cannot be null");

    if (valueKind != ProfileCharacteristicValueKind.INTERVAL_MIDPOINT) {
      throw new ApplicationValidationException("interval characteristic value kind expected");
    }
  }
}
