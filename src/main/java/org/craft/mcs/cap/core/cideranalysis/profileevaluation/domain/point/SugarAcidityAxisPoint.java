package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.point;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.MeasurementValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record SugarAcidityAxisPoint(
    @NonNull BigDecimal value,
    @NonNull MeasurementValueKind valueKind,
    @Nullable BigDecimal min,
    @Nullable BigDecimal max,
    @Nullable BigDecimal standardDeviation,
    @Nullable Integer sampleSize
) {

  public SugarAcidityAxisPoint {
    requireNonNull(value, "axis point value cannot be null");
    requireNonNull(valueKind, "axis point value kind cannot be null");
  }
}
