package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.normalization;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring.ProfileEvaluationAxis;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.jspecify.annotations.NonNull;


/**
 * Built-in numerical calibration ranges for profile-target scoring.
 *
 * <p>The inner breakpoints calibrate the piecewise distance function. They are not cider-apple
 * classification thresholds even where a current value happens to coincide with one.</p>
 */
public final class BuiltInProfileAxisRanges {

  private static final BigDecimal SPECIFIC_GRAVITY_MIN = new BigDecimal("1.020");
  private static final BigDecimal SPECIFIC_GRAVITY_MAX = new BigDecimal("1.080");
  private static final BigDecimal SPECIFIC_GRAVITY_BREAKPOINT_LOW = new BigDecimal("1.045");
  private static final BigDecimal SPECIFIC_GRAVITY_BREAKPOINT_HIGH = new BigDecimal("1.060");
  private static final BigDecimal TITRATABLE_ACIDITY_MIN = BigDecimal.ZERO;
  private static final BigDecimal TITRATABLE_ACIDITY_MAX = new BigDecimal("10");
  private static final BigDecimal TITRATABLE_ACIDITY_BREAKPOINT_LOW = new BigDecimal("4.5");
  private static final BigDecimal TITRATABLE_ACIDITY_BREAKPOINT_HIGH = new BigDecimal("7.5");
  private static final BigDecimal TANNIN_MIN = TanninValue.minValue();
  private static final BigDecimal TANNIN_MAX = TanninValue.maxValue();
  private static final BigDecimal TANNIN_BREAKPOINT_LOW = new BigDecimal("1500");
  private static final BigDecimal TANNIN_BREAKPOINT_HIGH = new BigDecimal("2500");
  private static final BigDecimal CIDER_BLENDING_V1_SPECIFIC_GRAVITY_MIN = new BigDecimal("1.035");
  private static final BigDecimal CIDER_BLENDING_V1_SPECIFIC_GRAVITY_MAX = new BigDecimal("1.075");
  private static final BigDecimal CIDER_BLENDING_V1_TITRATABLE_ACIDITY_MIN = new BigDecimal("2.0");
  private static final BigDecimal CIDER_BLENDING_V1_TITRATABLE_ACIDITY_MAX = new BigDecimal("9.0");
  private static final BigDecimal CIDER_BLENDING_V1_TANNIN_MIN = new BigDecimal("500");
  private static final BigDecimal CIDER_BLENDING_V1_TANNIN_MAX = new BigDecimal("3500");

  private BuiltInProfileAxisRanges() {
  }

  public static @NonNull List<@NonNull ProfileAxisNormalizationRange> fullRanges() {
    return List.of(
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.SPECIFIC_GRAVITY,
            SPECIFIC_GRAVITY_MIN,
            SPECIFIC_GRAVITY_MAX
        ),
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.TITRATABLE_ACIDITY,
            TITRATABLE_ACIDITY_MIN,
            TITRATABLE_ACIDITY_MAX
        ),
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.TANNIN,
            TANNIN_MIN,
            TANNIN_MAX
        )
    );
  }

  /**
   * Returns CAP's first versioned effective ranges for cider-blending target distance.
   *
   * <p>These are operational scoring ranges rather than physical limits or classification
   * thresholds.</p>
   */
  public static @NonNull List<@NonNull ProfileAxisNormalizationRange> ciderBlendingEffectiveRangesV1() {
    return List.of(
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.SPECIFIC_GRAVITY,
            CIDER_BLENDING_V1_SPECIFIC_GRAVITY_MIN,
            CIDER_BLENDING_V1_SPECIFIC_GRAVITY_MAX
        ),
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.TITRATABLE_ACIDITY,
            CIDER_BLENDING_V1_TITRATABLE_ACIDITY_MIN,
            CIDER_BLENDING_V1_TITRATABLE_ACIDITY_MAX
        ),
        new ProfileAxisNormalizationRange(
            ProfileEvaluationAxis.TANNIN,
            CIDER_BLENDING_V1_TANNIN_MIN,
            CIDER_BLENDING_V1_TANNIN_MAX
        )
    );
  }

  public static @NonNull List<@NonNull BigDecimal> specificGravityScoringBreakpoints() {
    return List.of(
        SPECIFIC_GRAVITY_MIN,
        SPECIFIC_GRAVITY_BREAKPOINT_LOW,
        SPECIFIC_GRAVITY_BREAKPOINT_HIGH,
        SPECIFIC_GRAVITY_MAX
    );
  }

  public static @NonNull List<@NonNull BigDecimal> titratableAcidityScoringBreakpoints() {
    return List.of(
        TITRATABLE_ACIDITY_MIN,
        TITRATABLE_ACIDITY_BREAKPOINT_LOW,
        TITRATABLE_ACIDITY_BREAKPOINT_HIGH,
        TITRATABLE_ACIDITY_MAX
    );
  }

  public static @NonNull List<@NonNull BigDecimal> tanninScoringBreakpoints() {
    return List.of(
        TANNIN_MIN,
        TANNIN_BREAKPOINT_LOW,
        TANNIN_BREAKPOINT_HIGH,
        TANNIN_MAX
    );
  }
}
