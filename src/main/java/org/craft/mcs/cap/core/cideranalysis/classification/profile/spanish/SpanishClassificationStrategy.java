package org.craft.mcs.cap.core.cideranalysis.classification.profile.spanish;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ClassificationStrategy;
import org.craft.mcs.cap.shared.kernel.chemical.axis.AxisRange;
import org.craft.mcs.cap.core.cideranalysis.classification.rule.ClassificationProfile;
import org.craft.mcs.cap.core.cideranalysis.classification.rule.ClassificationRule;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Spanish cider-apple classification strategy.
 *
 * <p>The strategy maps acidity/tannin threshold combinations to Spanish cider terms such as acida,
 * semiacida, dulce, dulce amarga and amarga. Sugar is not part of this profile, so rule matching is
 * driven by acidity and tannin ranges.
 */
public final class SpanishClassificationStrategy
    implements ClassificationStrategy<SpanishCiderAppleClass> {

  private final SpanishThresholds thresholds;

  public SpanishClassificationStrategy(@NonNull SpanishThresholds thresholds) {
    this.thresholds = requireNonNull(thresholds);
  }
  private static final AxisRange HIGH_TANNIN = AxisRange.atLeast("2000");

  @Override
  public @NonNull String code() {
    return "SPANISH";
  }

  @Override
  public @NonNull ClassificationProfile<SpanishCiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(
            ClassificationRule.of(SpanishCiderAppleClass.ACIDA, null, thresholds.highAcid(), thresholds.lowTannin()),
            ClassificationRule.of(SpanishCiderAppleClass.SEMIACIDA, null, thresholds.mediumAcid(),
                thresholds.lowTannin()),
            ClassificationRule.of(SpanishCiderAppleClass.DULCE, null, thresholds.lowAcid(), thresholds.lowTannin()),
            ClassificationRule.of(SpanishCiderAppleClass.DULCE_AMARGA, null, thresholds.lowAcid(),
                thresholds.mediumTannin()),
            ClassificationRule.of(SpanishCiderAppleClass.AMARGA, null, thresholds.lowAcid(), HIGH_TANNIN)
        )
    );
  }
}
