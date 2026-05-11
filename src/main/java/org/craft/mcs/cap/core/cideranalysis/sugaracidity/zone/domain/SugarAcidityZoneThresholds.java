package org.craft.mcs.cap.core.cideranalysis.sugaracidity.zone.domain;

import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record SugarAcidityZoneThresholds(
    @NonNull AxisRange lowSugar,
    @NonNull AxisRange mediumSugar,
    @NonNull AxisRange highSugar,

    @NonNull AxisRange lowAcidity,
    @NonNull AxisRange mediumAcidity,
    @NonNull AxisRange highAcidity,

    @NonNull AxisRange mediumTannin
) {

  public SugarAcidityZoneThresholds {
    requireNonNull(lowSugar, "low sugar range cannot be null");
    requireNonNull(mediumSugar, "medium sugar range cannot be null");
    requireNonNull(highSugar, "high sugar range cannot be null");

    requireNonNull(lowAcidity, "low acidity range cannot be null");
    requireNonNull(mediumAcidity, "medium acidity range cannot be null");
    requireNonNull(highAcidity, "high acidity range cannot be null");

    requireNonNull(mediumTannin, "medium tannin range cannot be null");
  }
}
