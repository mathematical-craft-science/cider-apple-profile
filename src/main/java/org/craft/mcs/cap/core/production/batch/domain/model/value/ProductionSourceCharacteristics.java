package org.craft.mcs.cap.core.production.batch.domain.model.value;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Canonical characteristics captured from one material source at the Production boundary.
 *
 * <p>The model belongs to the source-consumption scenario. Mathematical composition adapters
 * translate it to their own input model without exposing profile-analysis types to Production.</p>
 */
public record ProductionSourceCharacteristics(
    @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
    @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<TanninValue> tannin,
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {

  public ProductionSourceCharacteristics {
    requireNonNull(specificGravity, "production source specific gravity cannot be null");
    requireNonNull(titratableAcidity, "production source titratable acidity cannot be null");
  }
}
