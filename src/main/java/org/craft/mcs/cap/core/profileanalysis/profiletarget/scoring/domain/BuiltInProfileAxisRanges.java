package org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.BuiltInCiderProfileAxisBoundaries;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneThresholds;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class BuiltInProfileAxisRanges {

  private BuiltInProfileAxisRanges() {
  }

  public static @NonNull List<@NonNull ProfileAxisNormalizationRange> fullRanges() {
    return List.of(
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.SUGAR,
            BuiltInCiderProfileAxisBoundaries.SUGAR_MIN,
            BuiltInCiderProfileAxisBoundaries.SUGAR_MAX
        ),
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.ACIDITY,
            BuiltInCiderProfileAxisBoundaries.ACIDITY_MIN,
            BuiltInCiderProfileAxisBoundaries.ACIDITY_MAX
        ),
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.TANNIN,
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
