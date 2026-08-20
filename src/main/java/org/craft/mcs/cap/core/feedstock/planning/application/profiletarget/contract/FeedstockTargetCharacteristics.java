package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Typed Feedstock characteristics used by target-fit planning. */
public record FeedstockTargetCharacteristics(
    @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
    @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<TanninValue> tannin
) {

  public FeedstockTargetCharacteristics {
    requireNonNull(specificGravity, "feedstock target specific gravity cannot be null");
    requireNonNull(titratableAcidity, "feedstock target titratable acidity cannot be null");
  }
}
