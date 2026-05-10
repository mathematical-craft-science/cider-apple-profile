package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.domain.classification.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;


@Schema(description = "Exact graph-axis point value.")
public record ExactSugarAcidityAxisPointResponse(
    @Schema(description = "Axis value.", example = "1.052") @NonNull BigDecimal value
) implements SugarAcidityAxisPointResponse {

  @Override
  @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "EXACT")
  public @NonNull MeasurementValueKind valueKind() {
    return MeasurementValueKind.EXACT;
  }
}
