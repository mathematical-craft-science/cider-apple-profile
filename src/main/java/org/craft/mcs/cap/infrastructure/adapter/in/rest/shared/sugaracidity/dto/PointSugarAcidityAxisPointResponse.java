package org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Schema(description = "Point graph-axis value.")
public record PointSugarAcidityAxisPointResponse(
    @Schema(description = "Axis value.", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull DecimalQuantityResponse value,
    @Schema(
        description = "Axis point value discriminator.",
        implementation = AxisValueKind.class,
        example = "POINT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NonNull AxisValueKind valueKind
) implements SugarAcidityAxisPointResponse {

  public PointSugarAcidityAxisPointResponse(
      @NonNull BigDecimal value,
      @NonNull String unit
  ) {
    this(new DecimalQuantityResponse(value, unit), AxisValueKind.POINT);
  }

  public PointSugarAcidityAxisPointResponse {
    requireNonNull(value, "point axis value cannot be null");
    requireNonNull(valueKind, "point axis value kind cannot be null");

    if (valueKind != AxisValueKind.POINT) {
      throw new ApplicationValidationException("point axis value kind expected");
    }
  }
}
