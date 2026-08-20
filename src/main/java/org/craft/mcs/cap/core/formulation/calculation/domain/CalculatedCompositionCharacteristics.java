package org.craft.mcs.cap.core.formulation.calculation.domain;

import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Canonical point values calculated for one source composition. */
@ValueObject
public record CalculatedCompositionCharacteristics(
    @NonNull SpecificGravityValue specificGravity,
    @NonNull TitratableAcidityValue titratableAcidity,
    @Nullable TanninValue tannin,
    @Nullable YeastAssimilableNitrogenValue yeastAssimilableNitrogen
) {

  public CalculatedCompositionCharacteristics {
    requireNonNull(specificGravity, "calculated composition specific gravity cannot be null");
    requireNonNull(titratableAcidity, "calculated composition titratable acidity cannot be null");
  }
}
