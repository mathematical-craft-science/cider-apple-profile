package org.craft.mcs.cap.shared.domain.sugaracidity.plot;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityGraphRange(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public SugarAcidityGraphRange {
    requireNonNull(min, "graph range minimum cannot be null");
    requireNonNull(max, "graph range maximum cannot be null");
  }
}
