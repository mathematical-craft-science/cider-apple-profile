package org.craft.mcs.cap.shared.domain.classification.axis;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record AxisValue(
    @NonNull ChemicalAxis axis,
    @NonNull BigDecimal value,
    @NonNull MeasurementValueKind valueKind,
    @Nullable BigDecimal min,
    @Nullable BigDecimal max,
    @Nullable BigDecimal standardDeviation,
    @Nullable Integer sampleSize
) {

  public AxisValue {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    requireNonNull(valueKind, "measurement value kind cannot be null");
  }
}
