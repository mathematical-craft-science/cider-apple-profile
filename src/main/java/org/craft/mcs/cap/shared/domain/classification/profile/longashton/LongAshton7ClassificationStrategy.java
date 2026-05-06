package org.craft.mcs.cap.shared.domain.classification.profile.longashton;

import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.core.AxisRange;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationProfile;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationRule;
import org.craft.mcs.cap.shared.domain.classification.core.ClassificationStrategy;
import org.jspecify.annotations.NonNull;


public final class LongAshton7ClassificationStrategy
    implements ClassificationStrategy<LongAshton7CiderAppleClass> {

  private static final AxisRange LOW_ACID = AxisRange.lessThan("4.5");
  private static final AxisRange HIGH_ACID = AxisRange.atLeast("4.5");

  @Override
  public @NonNull String code() {
    return "LONG_ASHTON_7";
  }

  @Override
  public @NonNull ClassificationProfile<LongAshton7CiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(
            ClassificationRule.of(LongAshton7CiderAppleClass.SWEET, null, LOW_ACID, AxisRange.lessThan("1000")),
            ClassificationRule.of(LongAshton7CiderAppleClass.MILD_BITTERSWEET, null, LOW_ACID,
                AxisRange.between("1000", "1500")),
            ClassificationRule.of(LongAshton7CiderAppleClass.MEDIUM_BITTERSWEET, null, LOW_ACID,
                AxisRange.between("1500", "2000")),
            ClassificationRule.of(LongAshton7CiderAppleClass.FULL_BITTERSWEET, null, LOW_ACID,
                AxisRange.atLeast("2000")),
            ClassificationRule.of(LongAshton7CiderAppleClass.MEDIUM_SHARP, null, HIGH_ACID, AxisRange.lessThan("2000")),
            ClassificationRule.of(LongAshton7CiderAppleClass.FULL_SHARP, null, HIGH_ACID,
                AxisRange.between("2000", "3000")),
            ClassificationRule.of(LongAshton7CiderAppleClass.BITTERSHARP, null, HIGH_ACID, AxisRange.atLeast("3000"))
        )
    );
  }
}
