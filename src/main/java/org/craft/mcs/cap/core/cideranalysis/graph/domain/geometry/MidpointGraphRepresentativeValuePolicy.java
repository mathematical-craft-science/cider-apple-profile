package org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Uses direct point values and exact domain midpoints of intervals as graph coordinates.
 */
public final class MidpointGraphRepresentativeValuePolicy implements GraphRepresentativeValuePolicy {

  @Override
  public @NonNull SpecificGravityValue specificGravity(
      @NonNull CharacteristicValue<SpecificGravityValue> value
  ) {
    return SpecificGravityValue.of(midpoint(value));
  }

  @Override
  public @NonNull TitratableAcidityValue titratableAcidity(
      @NonNull CharacteristicValue<TitratableAcidityValue> value
  ) {
    return TitratableAcidityValue.of(midpoint(value));
  }
}
