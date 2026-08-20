package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget.contract;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Typed Apple reference characteristics used by target-fit analysis. */
public record AppleTargetCharacteristics(
    @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
    @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<TanninValue> tannin
) {

  public AppleTargetCharacteristics {
    requireNonNull(specificGravity, "apple target specific gravity cannot be null");
    requireNonNull(titratableAcidity, "apple target titratable acidity cannot be null");
  }
}
