package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Profile characteristic reported as one point value.")
public record PointProfileCharacteristicValueResponse(
    @Schema(description = "Characteristic value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "Characteristic projection kind.",
        type = "string",
        allowableValues = "POINT",
        example = "POINT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AxisValueKind valueKind
) implements ProfileCharacteristicValueResponse {

  public PointProfileCharacteristicValueResponse(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    this(new DecimalQuantityResponse(value, unit), AxisValueKind.POINT);
  }

  public PointProfileCharacteristicValueResponse {
    requireNonNull(value, "point characteristic value cannot be null");
    requireNonNull(valueKind, "point characteristic value kind cannot be null");

    if (valueKind != AxisValueKind.POINT) {
      throw new ApplicationValidationException("point characteristic value kind expected");
    }
  }
}
