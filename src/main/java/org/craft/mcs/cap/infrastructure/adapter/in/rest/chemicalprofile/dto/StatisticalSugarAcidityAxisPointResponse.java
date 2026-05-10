package org.craft.mcs.cap.infrastructure.adapter.in.rest.chemicalprofile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.domain.classification.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Statistical graph-axis point represented by mean or median value.")
public record StatisticalSugarAcidityAxisPointResponse(
    @Schema(description = "Axis value used as graph coordinate.", example = "5.2") @NonNull BigDecimal value,
    @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "STATISTICAL_MEAN")
    @NonNull MeasurementValueKind valueKind,
    @Schema(description = "Standard deviation, when available.", example = "0.2") @Nullable BigDecimal standardDeviation,
    @Schema(description = "Number of measurements in the statistical sample.", example = "3") @Nullable Integer sampleSize
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
