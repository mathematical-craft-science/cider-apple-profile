package org.craft.mcs.cap.core.profileanalysis.classification.domain.profile.french;

import java.util.List;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.engine.ClassificationStrategy;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationProfile;
import org.craft.mcs.cap.core.profileanalysis.classification.domain.rule.ClassificationRule;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * French cider-apple classification strategy.
 *
 * <p>The strategy maps acidity/tannin threshold combinations to traditional French terms such as
 * douce, douce amere, amere, acidulee, aigre and aigre amere. Sugar is not part of this profile,
 * so the generated rules leave the Specific Gravity range empty and classify by acidity and tannin only.
 */
public final class FrenchClassificationStrategy
    implements ClassificationStrategy<FrenchCiderAppleClass> {

  private final FrenchThresholds thresholds;

  public FrenchClassificationStrategy(@NonNull FrenchThresholds thresholds) {
    this.thresholds = requireNonNull(thresholds);
  }

  @Override
  public @NonNull String code() {
    return "FRENCH";
  }

  @Override
  public @NonNull Set<@NonNull ClassificationCharacteristic> requiredCharacteristics() {
    return Set.of(
        ClassificationCharacteristic.ACIDITY,
        ClassificationCharacteristic.TANNIN
    );
  }

  @Override
  public @NonNull ClassificationProfile<FrenchCiderAppleClass> profile() {
    return new ClassificationProfile<>(
        code(),
        List.of(

            // LOW ACID
            ClassificationRule.of(FrenchCiderAppleClass.DOUCE, null, thresholds.lowAcid(), thresholds.lowTannin()),
            ClassificationRule.of(FrenchCiderAppleClass.DOUCE_AMERE, null, thresholds.lowAcid(),
                thresholds.mediumTannin()),
            ClassificationRule.of(FrenchCiderAppleClass.AMERE, null, thresholds.lowAcid(), thresholds.highTannin()),

            // MEDIUM ACID
            ClassificationRule.of(FrenchCiderAppleClass.ACIDULEE, null, thresholds.mediumAcid(),
                thresholds.lowTannin()),

            // HIGH ACID
            ClassificationRule.of(FrenchCiderAppleClass.AIGRE, null, thresholds.highAcid(), thresholds.lowTannin()),
            ClassificationRule.of(FrenchCiderAppleClass.AIGRE_AMERE, null, thresholds.highAcid(),
                thresholds.highTannin())
        )
    );
  }
}
