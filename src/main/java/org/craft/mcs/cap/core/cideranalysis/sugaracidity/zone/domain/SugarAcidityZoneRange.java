package org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isBetweenInclusive;
import static org.craft.mcs.cap.shared.kernel.math.DecimalComparisons.isGreaterThan;


public record SugarAcidityZoneRange(
    @NonNull BigDecimal min,
    @NonNull BigDecimal max
) {

  public SugarAcidityZoneRange {
    requireNonNull(min, "zone range minimum cannot be null");
    requireNonNull(max, "zone range maximum cannot be null");
    if (isGreaterThan(min, max)) {
      throw new DomainRuleViolationException("zone range minimum cannot be greater than maximum");
    }
  }

  public boolean contains(@NonNull BigDecimal value) {
    requireNonNull(value, "zone range value cannot be null");
    return isBetweenInclusive(value, min, max);
  }
}
