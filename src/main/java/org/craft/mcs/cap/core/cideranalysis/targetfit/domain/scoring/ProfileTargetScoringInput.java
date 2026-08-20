package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Complete typed input for profile target distance and contribution-role assessment.
 *
 * <p>SG and TA are required. Tannin is independently optional and YAN is deliberately absent
 * because fermentation readiness must not affect target distance.</p>
 */
@ValueObject
public record ProfileTargetScoringInput(
    @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
    @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<TanninValue> tannin
) {

  public ProfileTargetScoringInput {
    requireNonNull(specificGravity, "target-scoring specific gravity cannot be null");
    requireNonNull(titratableAcidity, "target-scoring titratable acidity cannot be null");
  }

  public @NonNull ProfileTargetScoringPoint representativePoint(
      @NonNull ProfileTargetIntervalPolicy intervalPolicy
  ) {
    return requireNonNull(intervalPolicy, "profile target interval policy cannot be null")
        .representativePoint(this);
  }

  public static @NonNull ProfileTargetScoringInput point(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity,
      @Nullable TanninValue tannin
  ) {
    return new ProfileTargetScoringInput(
        PointCharacteristicValue.of(
            requireNonNull(specificGravity, "target-scoring specific gravity cannot be null")
        ),
        PointCharacteristicValue.of(
            requireNonNull(titratableAcidity, "target-scoring titratable acidity cannot be null")
        ),
        tannin == null ? null : PointCharacteristicValue.of(tannin)
    );
  }
}
