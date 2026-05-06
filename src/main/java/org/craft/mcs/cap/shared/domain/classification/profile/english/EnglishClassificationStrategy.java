package org.craft.mcs.cap.shared.domain.classification.profile.english;

import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.core.AxisRange;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationProfile;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationRule;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationStrategy;
import org.jspecify.annotations.NonNull;


public final class EnglishClassificationStrategy
    implements ClassificationStrategy<EnglishCiderAppleClass> {

  private static final AxisRange LOW_ACID = AxisRange.lessThan("4.5");
  private static final AxisRange HIGH_ACID = AxisRange.atLeast("4.5");
  private static final AxisRange LOW_TANNIN = AxisRange.lessThan("2000");
  private static final AxisRange HIGH_TANNIN = AxisRange.atLeast("2000");

  @Override
  public @NonNull String code() {
    return "ENGLISH";
  }

  @Override
  public @NonNull ClassificationProfile<EnglishCiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(
            ClassificationRule.of(EnglishCiderAppleClass.SWEET, null, LOW_ACID, LOW_TANNIN),
            ClassificationRule.of(EnglishCiderAppleClass.SHARP, null, HIGH_ACID, LOW_TANNIN),
            ClassificationRule.of(EnglishCiderAppleClass.BITTERSWEET, null, LOW_ACID, HIGH_TANNIN),
            ClassificationRule.of(EnglishCiderAppleClass.BITTERSHARP, null, HIGH_ACID, HIGH_TANNIN)
        )
    );
  }
}
