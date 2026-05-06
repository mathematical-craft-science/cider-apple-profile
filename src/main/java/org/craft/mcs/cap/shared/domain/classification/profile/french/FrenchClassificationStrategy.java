package org.craft.mcs.cap.shared.domain.classification.profile.french;

import java.util.List;
import org.craft.mcs.cap.shared.domain.classification.engine.ClassificationStrategy;
import org.craft.mcs.cap.shared.domain.classification.rule.ClassificationProfile;
import org.craft.mcs.cap.shared.domain.classification.rule.ClassificationRule;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


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
