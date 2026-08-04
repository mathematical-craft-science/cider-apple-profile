package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Typed canonical characteristics supplied by one composition source. */
@ValueObject
public record CompositionSourceCharacteristics(
    @NonNull CharacteristicValue<SpecificGravityValue> specificGravity,
    @NonNull CharacteristicValue<TitratableAcidityValue> titratableAcidity,
    @Nullable CharacteristicValue<TanninValue> tannin,
    @Nullable CharacteristicValue<YeastAssimilableNitrogenValue> yeastAssimilableNitrogen
) {

  public CompositionSourceCharacteristics {
    requireNonNull(specificGravity, "composition source specific gravity cannot be null");
    requireNonNull(titratableAcidity, "composition source titratable acidity cannot be null");
  }
}
