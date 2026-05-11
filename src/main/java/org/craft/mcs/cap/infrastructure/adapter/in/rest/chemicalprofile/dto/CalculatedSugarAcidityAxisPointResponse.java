package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Calculated graph-axis point value.")
public record CalculatedSugarAcidityAxisPointResponse(
    @Schema(description = "Calculated axis value.", example = "1.052") @NonNull BigDecimal value
) implements SugarAcidityAxisPointResponse {

  @Override
  @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "CALCULATED")
  public @NonNull MeasurementValueKind valueKind() {
    return MeasurementValueKind.CALCULATED;
  }
}
