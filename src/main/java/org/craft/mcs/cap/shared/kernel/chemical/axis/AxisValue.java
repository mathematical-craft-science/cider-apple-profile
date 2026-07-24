package org.craft.mcs.cap.shared.kernel.chemical.axis;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record AxisValue(
    @NonNull ChemicalAxis axis,
    @NonNull BigDecimal value,
    @NonNull AxisValueKind valueKind,
    @Nullable BigDecimal min,
    @Nullable BigDecimal max
) {

  public AxisValue {
    requireNonNull(axis, "chemical axis cannot be null");
    requireNonNull(value, "axis value cannot be null");
    requireNonNull(valueKind, "axis value kind cannot be null");
  }
}
