package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Calculated graph-axis point value.")
public record CalculatedSugarAcidityAxisPointResponse(
    @Schema(description = "Calculated axis value.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse value,
    @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "CALCULATED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueKind valueKind
) implements SugarAcidityAxisPointResponse {

  public CalculatedSugarAcidityAxisPointResponse(@NonNull BigDecimal value, @NonNull String unit) {
    this(new DecimalQuantityResponse(value, unit), MeasurementValueKind.CALCULATED);
  }

  public CalculatedSugarAcidityAxisPointResponse {
    requireNonNull(value, "calculated axis point value cannot be null");
    requireNonNull(valueKind, "calculated axis point value kind cannot be null");

    if (valueKind != MeasurementValueKind.CALCULATED) {
      throw new IllegalArgumentException("calculated axis point value kind expected");
    }
  }
}
