package org.craft.mcs.cap.core.cideranalysis.classification.profile.longashton;

import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record LongAshton7Thresholds(
    @NonNull AxisRange lowAcid,
    @NonNull AxisRange highAcid,
    @NonNull AxisRange veryLowTannin,
    @NonNull AxisRange lowTannin,
    @NonNull AxisRange mediumTannin,
    @NonNull AxisRange highTannin,
    @NonNull AxisRange sharpLowTannin,
    @NonNull AxisRange sharpMediumTannin,
    @NonNull AxisRange sharpHighTannin
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
  }
}
