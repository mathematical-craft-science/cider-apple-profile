package org.craft.mcs.cap.core.profileanalysis.sugaracidity.point.domain;

import java.math.BigDecimal;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisValueKind;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record SugarAcidityAxisPoint(
    @NonNull BigDecimal value,
    @NonNull AxisValueKind valueKind,
    @Nullable BigDecimal min,
    @Nullable BigDecimal max
) {

  public SugarAcidityAxisPoint {
    requireNonNull(value, "axis point value cannot be null");
    requireNonNull(valueKind, "axis point value kind cannot be null");
  }
}
