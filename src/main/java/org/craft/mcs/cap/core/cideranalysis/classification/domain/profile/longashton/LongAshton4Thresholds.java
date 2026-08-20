package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule
    .ClassificationValuePartitions.requireComplete;


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
    requireComplete("Long Ashton 4 acidity", List.of(lowAcid, highAcid));
    requireComplete("Long Ashton 4 tannin", List.of(lowTannin, highTannin));
  }
}
