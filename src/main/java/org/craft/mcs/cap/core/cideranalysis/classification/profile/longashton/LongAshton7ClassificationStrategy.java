package org.craft.mcs.cap.core.cideranalysis.classification.profile.longashton;

import java.util.List;
import org.craft.mcs.cap.core.cideranalysis.classification.engine.ClassificationStrategy;
import org.craft.mcs.cap.core.cideranalysis.classification.rule.ClassificationProfile;
import org.craft.mcs.cap.core.cideranalysis.classification.rule.ClassificationRule;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Long Ashton seven-class classification strategy.
 *
 * <p>This strategy refines the four-class English vocabulary by splitting bittersweet and sharp
 * categories into practical tannin intensity bands. It is useful when a cider maker needs more
 * descriptive local terminology than the coarse four-class profile.
 */
public final class LongAshton7ClassificationStrategy
    implements ClassificationStrategy<LongAshton7CiderAppleClass> {

  private final LongAshton7Thresholds thresholds;

  public LongAshton7ClassificationStrategy(@NonNull LongAshton7Thresholds thresholds) {
    this.thresholds = requireNonNull(thresholds);
  }

  @Override
  public @NonNull String code() {
    return "LONG_ASHTON_7";
  }

  @Override
  public @NonNull ClassificationProfile<LongAshton7CiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(
            ClassificationRule.of(LongAshton7CiderAppleClass.SWEET, null, thresholds.lowAcid(),
                thresholds.veryLowTannin()),
            ClassificationRule.of(LongAshton7CiderAppleClass.MILD_BITTERSWEET, null, thresholds.lowAcid(),
                thresholds.lowTannin()),
            ClassificationRule.of(LongAshton7CiderAppleClass.MEDIUM_BITTERSWEET, null, thresholds.lowAcid(),
                thresholds.mediumTannin()),
            ClassificationRule.of(LongAshton7CiderAppleClass.FULL_BITTERSWEET, null, thresholds.lowAcid(),
                thresholds.highTannin()),

            ClassificationRule.of(LongAshton7CiderAppleClass.MEDIUM_SHARP, null, thresholds.highAcid(),
                thresholds.sharpLowTannin()),
            ClassificationRule.of(LongAshton7CiderAppleClass.FULL_SHARP, null, thresholds.highAcid(),
                thresholds.sharpMediumTannin()),
            ClassificationRule.of(LongAshton7CiderAppleClass.BITTERSHARP, null, thresholds.highAcid(),
                thresholds.sharpHighTannin())
        )
    );
  }
}
