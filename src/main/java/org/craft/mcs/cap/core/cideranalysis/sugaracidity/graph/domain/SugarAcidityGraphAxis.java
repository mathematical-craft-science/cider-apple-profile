package org.craft.mcs.cap.core.cideranalysis.sugaracidity.graph.domain;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityGraphAxis(
    @NonNull String code,
    @NonNull String label,
    @NonNull String unit,
    @NonNull BigDecimal min,
    @NonNull BigDecimal max,
    @NonNull List<@NonNull BigDecimal> thresholdLines
) {

  public SugarAcidityGraphAxis {
    requireNonNull(code, "axis code cannot be null");
    requireNonNull(label, "axis label cannot be null");
    requireNonNull(unit, "axis unit cannot be null");
    requireNonNull(min, "axis minimum cannot be null");
    requireNonNull(max, "axis maximum cannot be null");
    thresholdLines = List.copyOf(requireNonNull(thresholdLines, "axis threshold lines cannot be null"));
  }
}
