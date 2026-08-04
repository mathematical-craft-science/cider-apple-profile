package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.longashton;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationProfile;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationRule;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Long Ashton four-class classification strategy.
 *
 * <p>This strategy implements the compact English cider apple vocabulary: sweet, sharp,
 * bittersweet and bittersharp. It classifies by acidity and tannin threshold quadrants and leaves
 * sugar out of rule matching.
 */
public final class LongAshton4ClassificationStrategy
    implements ClassificationStrategy<LongAshton4CiderAppleClass> {

  private final LongAshton4Thresholds thresholds;

  public LongAshton4ClassificationStrategy(@NonNull LongAshton4Thresholds thresholds) {
    this.thresholds = requireNonNull(thresholds);
  }

  @Override
  public @NonNull String code() {
    return "LONG_ASHTON_4";
  }

  @Override
  public @NonNull Set<@NonNull ClassificationCharacteristic> requiredCharacteristics() {
    return Set.of(
        ClassificationCharacteristic.ACIDITY,
        ClassificationCharacteristic.TANNIN
    );
  }

  @Override
  public @NonNull ClassificationProfile<LongAshton4CiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(
            ClassificationRule.of(LongAshton4CiderAppleClass.SWEET, null, thresholds.lowAcid(), thresholds.lowTannin()),
            ClassificationRule.of(LongAshton4CiderAppleClass.SHARP, null, thresholds.highAcid(),
                thresholds.lowTannin()),
            ClassificationRule.of(LongAshton4CiderAppleClass.BITTERSWEET, null, thresholds.lowAcid(),
                thresholds.highTannin()),
            ClassificationRule.of(LongAshton4CiderAppleClass.BITTERSHARP, null, thresholds.highAcid(),
                thresholds.highTannin())
        )
    );
  }
}
