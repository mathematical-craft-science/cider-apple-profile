package org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityZoneRange(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public SugarAcidityZoneRange {
    requireNonNull(min, "zone range minimum cannot be null");
    requireNonNull(max, "zone range maximum cannot be null");
    if (min.compareTo(max) > 0) {
      throw new DomainRuleViolationException("zone range minimum cannot be greater than maximum");
    }
  }

  public boolean contains(@NonNull BigDecimal value) {
    requireNonNull(value, "zone range value cannot be null");
    return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
  }
}
