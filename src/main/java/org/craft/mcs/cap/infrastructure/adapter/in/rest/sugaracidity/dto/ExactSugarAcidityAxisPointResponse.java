package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Exact graph-axis point value.")
public record ExactSugarAcidityAxisPointResponse(
    @Schema(description = "Axis value.", example = "1.052") @NonNull BigDecimal value,
    @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "EXACT")
    @NonNull MeasurementValueKind valueKind
) implements SugarAcidityAxisPointResponse {

  public ExactSugarAcidityAxisPointResponse(@NonNull BigDecimal value) {
    this(value, MeasurementValueKind.EXACT);
  }

  public ExactSugarAcidityAxisPointResponse {
    requireNonNull(value, "exact axis point value cannot be null");
    requireNonNull(valueKind, "exact axis point value kind cannot be null");

    if (valueKind != MeasurementValueKind.EXACT) {
      throw new IllegalArgumentException("exact axis point value kind expected");
    }
  }
}
