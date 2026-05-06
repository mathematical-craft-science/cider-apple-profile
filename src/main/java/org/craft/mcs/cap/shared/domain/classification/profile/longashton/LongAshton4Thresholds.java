package org.craft.mcs.cap.shared.domain.classification.profile.longashton;

import org.craft.mcs.cap.shared.domain.classification.rule.AxisRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record LongAshton4Thresholds(
    @NonNull AxisRange lowAcid,
    @NonNull AxisRange highAcid,
    @NonNull AxisRange lowTannin,
    @NonNull AxisRange highTannin
) {

  public LongAshton4Thresholds {
    requireNonNull(lowAcid, "low acid range cannot be null");
    requireNonNull(highAcid, "high acid range cannot be null");
    requireNonNull(lowTannin, "low tannin range cannot be null");
    requireNonNull(highTannin, "high tannin range cannot be null");
  }
}
