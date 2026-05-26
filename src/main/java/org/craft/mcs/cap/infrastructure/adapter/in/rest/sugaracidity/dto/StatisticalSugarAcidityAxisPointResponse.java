package org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@Schema(description = "Statistical graph-axis point represented by mean or median value.")
public record StatisticalSugarAcidityAxisPointResponse(
    @Schema(description = "Axis value used as graph coordinate.", requiredMode = Schema.RequiredMode.REQUIRED) @NonNull DecimalQuantityResponse value,
    @Schema(description = "Axis point value discriminator.", implementation = MeasurementValueKind.class, example = "STATISTICAL_MEAN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull MeasurementValueKind valueKind,
    @Schema(description = "Standard deviation in canonical units, when available.", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable DecimalQuantityResponse standardDeviation,
    @Schema(description = "Number of measurements in the statistical sample.", example = "3", nullable = true, requiredMode = Schema.RequiredMode.NOT_REQUIRED) @Nullable Integer sampleSize
) implements SugarAcidityAxisPointResponse {

  public StatisticalSugarAcidityAxisPointResponse(
      @NonNull BigDecimal value,
      @NonNull MeasurementValueKind valueKind,
      @Nullable BigDecimal standardDeviation,
      @Nullable Integer sampleSize,
      @NonNull String unit
  ) {
    this(
        new DecimalQuantityResponse(value, unit),
        valueKind,
        standardDeviation == null ? null : new DecimalQuantityResponse(standardDeviation, unit),
        sampleSize
    );
  }

  public StatisticalSugarAcidityAxisPointResponse {
    requireNonNull(value, "statistical axis point value cannot be null");
    requireNonNull(valueKind, "statistical axis point value kind cannot be null");

    if (valueKind != MeasurementValueKind.STATISTICAL_MEAN
        && valueKind != MeasurementValueKind.STATISTICAL_MEDIAN) {
      throw new IllegalArgumentException("statistical axis point value kind expected");
    }
  }
}
