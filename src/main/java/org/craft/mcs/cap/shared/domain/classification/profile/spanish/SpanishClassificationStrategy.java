package org.craft.mcs.cap.shared.domain.classification.profile.spanish;

import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.engine.ClassificationStrategy;
import org.craft.mcs.cap.shared.domain.classification.rule.AxisRange;
import org.craft.mcs.cap.shared.domain.classification.rule.ClassificationProfile;
import org.craft.mcs.cap.shared.domain.classification.rule.ClassificationRule;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


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
