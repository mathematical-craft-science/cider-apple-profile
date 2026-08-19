package org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Typed current Feedstock characteristics exposed to an external material consumer. */
public record FeedstockMaterialCharacteristics(
    @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
    @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<TanninValue> tannin,
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {

  public FeedstockMaterialCharacteristics {
    requireNonNull(specificGravity, "feedstock material specific gravity cannot be null");
    requireNonNull(titratableAcidity, "feedstock material titratable acidity cannot be null");
  }
}
