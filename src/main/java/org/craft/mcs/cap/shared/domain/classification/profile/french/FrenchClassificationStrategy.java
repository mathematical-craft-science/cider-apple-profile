package org.craft.mcs.cap.shared.domain.classification.profile.french;

import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.core.AxisRange;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationProfile;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationRule;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationStrategy;
import org.jspecify.annotations.NonNull;


public final class FrenchClassificationStrategy
    implements ClassificationStrategy<FrenchCiderAppleClass> {

  private static final AxisRange LOW_TANNIN = AxisRange.lessThan("2000");
  private static final AxisRange HIGH_TANNIN = AxisRange.atLeast("2000");

  @Override
  public @NonNull String code() {
    return "FRENCH";
  }

  @Override
  public @NonNull ClassificationProfile<FrenchCiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(
            ClassificationRule.of(FrenchCiderAppleClass.DOUCE, null, AxisRange.lessThan("3.0"), LOW_TANNIN),
            ClassificationRule.of(FrenchCiderAppleClass.DOUCE_AMERE, null, AxisRange.lessThan("3.0"), HIGH_TANNIN),
            ClassificationRule.of(FrenchCiderAppleClass.AMERE, null, AxisRange.between("3.0", "4.5"), HIGH_TANNIN),
            ClassificationRule.of(FrenchCiderAppleClass.ACIDULEE, null, AxisRange.between("3.0", "4.5"), LOW_TANNIN),
            ClassificationRule.of(FrenchCiderAppleClass.AIGRE, null, AxisRange.atLeast("4.5"), LOW_TANNIN),
            ClassificationRule.of(FrenchCiderAppleClass.AIGRE_AMERE, null, AxisRange.atLeast("4.5"), HIGH_TANNIN)
        )
    );
  }
}
