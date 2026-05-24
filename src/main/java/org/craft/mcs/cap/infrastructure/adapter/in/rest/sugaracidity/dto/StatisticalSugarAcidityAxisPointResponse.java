package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Statistical graph-axis point represented by mean or median value.")
public record StatisticalSugarAcidityAxisPointResponse(
    @Schema(description = "Axis value used as graph coordinate.", example = "5.2", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull BigDecimal value,
    @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "STATISTICAL_MEAN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueKind valueKind,
    @Schema(description = "Standard deviation, when available.", example = "0.2", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable BigDecimal standardDeviation,
    @Schema(description = "Number of measurements in the statistical sample.", example = "3", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable Integer sampleSize
) implements SugarAcidityAxisPointResponse {

  public StatisticalSugarAcidityAxisPointResponse {
    requireNonNull(value, "statistical axis point value cannot be null");
    requireNonNull(valueKind, "statistical axis point value kind cannot be null");

    if (valueKind != MeasurementValueKind.STATISTICAL_MEAN
        && valueKind != MeasurementValueKind.STATISTICAL_MEDIAN) {
      throw new IllegalArgumentException("statistical axis point value kind expected");
    }
  }
}
