package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.longashton;

import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record LongAshton4Thresholds(
    @NonNull ClassificationValueRange lowAcid,
    @NonNull ClassificationValueRange highAcid,
    @NonNull ClassificationValueRange lowTannin,
    @NonNull ClassificationValueRange highTannin
) {

  public LongAshton4Thresholds {
    requireNonNull(lowAcid, "low acid range cannot be null");
    requireNonNull(highAcid, "high acid range cannot be null");
    requireNonNull(lowTannin, "low tannin range cannot be null");
    requireNonNull(highTannin, "high tannin range cannot be null");
  }
}
