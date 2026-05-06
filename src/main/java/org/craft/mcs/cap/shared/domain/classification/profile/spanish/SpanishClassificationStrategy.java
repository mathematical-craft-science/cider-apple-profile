package org.craft.mcs.cap.shared.domain.classification.profile.spanish;

import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.core.AxisRange;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationProfile;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationRule;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationStrategy;
import org.jspecify.annotations.NonNull;


public final class SpanishClassificationStrategy
    implements ClassificationStrategy<SpanishCiderAppleClass> {

  private static final AxisRange LOW_TANNIN = AxisRange.lessThan("1000");
  private static final AxisRange MEDIUM_TANNIN = AxisRange.between("1000", "2000");
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
            ClassificationRule.of(SpanishCiderAppleClass.ACIDA, null, AxisRange.atLeast("5.0"), LOW_TANNIN),
            ClassificationRule.of(SpanishCiderAppleClass.SEMIACIDA, null, AxisRange.between("3.0", "5.0"), LOW_TANNIN),
            ClassificationRule.of(SpanishCiderAppleClass.DULCE, null, AxisRange.lessThan("3.0"), LOW_TANNIN),
            ClassificationRule.of(SpanishCiderAppleClass.DULCE_AMARGA, null, AxisRange.lessThan("3.0"), MEDIUM_TANNIN),
            ClassificationRule.of(SpanishCiderAppleClass.AMARGA, null, AxisRange.lessThan("3.0"), HIGH_TANNIN)
        )
    );
  }
}
