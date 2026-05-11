package org.craft.mcs.cap.core.cideranalysis.classification.profile.northamerican;

import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record NorthAmericanThresholds(
    @NonNull AxisRange lowSugar,
    @NonNull AxisRange mediumSugar,
    @NonNull AxisRange highSugar,
    @NonNull AxisRange veryHighSugar,

    @NonNull AxisRange lowAcidity,
    @NonNull AxisRange mediumAcidity,
    @NonNull AxisRange highAcidity,
    @NonNull AxisRange veryHighAcidity,

    @NonNull AxisRange lowTannin,
    @NonNull AxisRange mediumTannin,
    @NonNull AxisRange highTannin
) {

  public NorthAmericanThresholds {
    requireNonNull(lowSugar);
    requireNonNull(mediumSugar);
    requireNonNull(highSugar);
    requireNonNull(veryHighSugar);

    requireNonNull(lowAcidity);
    requireNonNull(mediumAcidity);
    requireNonNull(highAcidity);
    requireNonNull(veryHighAcidity);

    requireNonNull(lowTannin);
    requireNonNull(mediumTannin);
    requireNonNull(highTannin);
  }
}
