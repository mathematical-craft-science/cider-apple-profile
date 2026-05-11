package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.axis.BuiltInCiderProfileAxisBoundaries;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneThresholds;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class BuiltInBlendProfileAxisRanges {

  private BuiltInBlendProfileAxisRanges() {
  }

  public static @NonNull List<@NonNull BlendProfileAxisNormalizationRange> fullRanges() {
    return List.of(
        new BlendProfileAxisNormalizationRange(
            BlendProfileAxis.SUGAR,
            BuiltInCiderProfileAxisBoundaries.SUGAR_MIN,
            BuiltInCiderProfileAxisBoundaries.SUGAR_MAX
        ),
        new BlendProfileAxisNormalizationRange(
            BlendProfileAxis.ACIDITY,
            BuiltInCiderProfileAxisBoundaries.ACIDITY_MIN,
            BuiltInCiderProfileAxisBoundaries.ACIDITY_MAX
        ),
        new BlendProfileAxisNormalizationRange(
            BlendProfileAxis.TANNIN,
            BuiltInCiderProfileAxisBoundaries.TANNIN_MIN,
            BuiltInCiderProfileAxisBoundaries.TANNIN_MAX
        )
    );
  }

  public static @NonNull List<@NonNull BigDecimal> sugarThresholdBreakpoints() {
    return BuiltInCiderProfileAxisBoundaries.sugarThresholdBreakpoints();
  }

  public static @NonNull List<@NonNull BigDecimal> acidityThresholdBreakpoints() {
    return BuiltInCiderProfileAxisBoundaries.acidityThresholdBreakpoints();
  }

  public static @NonNull List<@NonNull BigDecimal> tanninThresholdBreakpoints(
      @NonNull SugarAcidityZoneThresholds thresholds
  ) {
    requireNonNull(thresholds, "sugar-acidity zone thresholds cannot be null");
    return List.of(
        BuiltInCiderProfileAxisBoundaries.TANNIN_MIN,
        requireNonNull(thresholds.mediumTannin().lowerBoundary(), "medium tannin lower boundary cannot be null"),
        requireNonNull(thresholds.mediumTannin().upperBoundary(), "medium tannin upper boundary cannot be null"),
        BuiltInCiderProfileAxisBoundaries.TANNIN_MAX
    );
  }
}
