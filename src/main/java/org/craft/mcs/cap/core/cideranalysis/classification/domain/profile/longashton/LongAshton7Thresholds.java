package org.craft.mcs.cap.core.cideranalysis.classification.domain.profile.longashton;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.classification.domain.rule.ClassificationValueRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.cideranalysis.classification.domain.rule
    .ClassificationValuePartitions.requireComplete;


public record LongAshton7Thresholds(
    @NonNull ClassificationValueRange lowAcid,
    @NonNull ClassificationValueRange highAcid,
    @NonNull ClassificationValueRange veryLowTannin,
    @NonNull ClassificationValueRange lowTannin,
    @NonNull ClassificationValueRange mediumTannin,
    @NonNull ClassificationValueRange highTannin,
    @NonNull ClassificationValueRange sharpLowTannin,
    @NonNull ClassificationValueRange sharpMediumTannin,
    @NonNull ClassificationValueRange sharpHighTannin
) {

  public LongAshton7Thresholds {
    requireNonNull(lowAcid, "low acid range cannot be null");
    requireNonNull(highAcid, "high acid range cannot be null");
    requireNonNull(veryLowTannin, "very low tannin range cannot be null");
    requireNonNull(lowTannin, "low tannin range cannot be null");
    requireNonNull(mediumTannin, "medium tannin range cannot be null");
    requireNonNull(highTannin, "high tannin range cannot be null");
    requireNonNull(sharpLowTannin, "sharp low tannin range cannot be null");
    requireNonNull(sharpMediumTannin, "sharp medium tannin range cannot be null");
    requireNonNull(sharpHighTannin, "sharp high tannin range cannot be null");
    requireComplete("Long Ashton 7 acidity", List.of(lowAcid, highAcid));
    requireComplete(
        "Long Ashton 7 low-acidity tannin",
        List.of(veryLowTannin, lowTannin, mediumTannin, highTannin)
    );
    requireComplete(
        "Long Ashton 7 high-acidity tannin",
        List.of(sharpLowTannin, sharpMediumTannin, sharpHighTannin)
    );
  }
}
