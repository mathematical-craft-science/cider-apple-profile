package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profilecharacteristic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Calculated profile characteristic value.")
public record CalculatedProfileCharacteristicValueResponse(
    @Schema(
        description = "Calculated characteristic value.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "Characteristic projection kind.",
        type = "string",
        allowableValues = "CALCULATED",
        example = "CALCULATED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull ProfileCharacteristicValueKind valueKind
) implements ProfileCharacteristicValueResponse {

  public CalculatedProfileCharacteristicValueResponse(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    this(new DecimalQuantityResponse(value, unit), ProfileCharacteristicValueKind.CALCULATED);
  }

  public CalculatedProfileCharacteristicValueResponse {
    requireNonNull(value, "calculated characteristic value cannot be null");
    requireNonNull(valueKind, "calculated characteristic value kind cannot be null");

    if (valueKind != ProfileCharacteristicValueKind.CALCULATED) {
      throw new ApplicationValidationException("calculated characteristic value kind expected");
    }
  }
}
