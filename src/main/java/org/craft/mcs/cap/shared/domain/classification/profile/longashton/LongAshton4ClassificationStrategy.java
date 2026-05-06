package org.craft.mcs.cap.shared.domain.classification.profile.longashton;

import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.core.AxisRange;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationProfile;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationRule;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationStrategy;
import org.jspecify.annotations.NonNull;


public final class LongAshton4ClassificationStrategy
    implements ClassificationStrategy<LongAshton4CiderAppleClass> {

  private static final AxisRange LOW_ACID = AxisRange.lessThan("4.5");
  private static final AxisRange HIGH_ACID = AxisRange.atLeast("4.5");
  private static final AxisRange LOW_TANNIN = AxisRange.lessThan("2000");
  private static final AxisRange HIGH_TANNIN = AxisRange.atLeast("2000");

  @Override
  public @NonNull String code() {
    return "LONG_ASHTON_4";
  }

  @Override
  public @NonNull ClassificationProfile<LongAshton4CiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(
            ClassificationRule.of(LongAshton4CiderAppleClass.SWEET, null, LOW_ACID, LOW_TANNIN),
            ClassificationRule.of(LongAshton4CiderAppleClass.SHARP, null, HIGH_ACID, LOW_TANNIN),
            ClassificationRule.of(LongAshton4CiderAppleClass.BITTERSWEET, null, LOW_ACID, HIGH_TANNIN),
            ClassificationRule.of(LongAshton4CiderAppleClass.BITTERSHARP, null, HIGH_ACID, HIGH_TANNIN)
        )
    );
  }
}
