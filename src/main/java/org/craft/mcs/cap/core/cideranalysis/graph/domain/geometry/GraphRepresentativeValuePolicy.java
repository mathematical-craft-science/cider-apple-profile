package org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;


/** Selects one representative value from a point or interval for an SG/TA graph projection. */
public interface GraphRepresentativeValuePolicy {

  @NonNull SpecificGravityValue specificGravity(
      @NonNull CharacteristicValue<SpecificGravityValue> value
  );

  @NonNull TitratableAcidityValue titratableAcidity(
      @NonNull CharacteristicValue<TitratableAcidityValue> value
  );

}
