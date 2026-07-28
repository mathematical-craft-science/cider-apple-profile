package org.craft.mcs.cap.shared.kernel.sugar;

import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.IntervalCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.characteristic.PointCharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Preserves point or interval shape while projecting sugar concentration to canonical SG. */
public final class SugarCharacteristicValues {

  private SugarCharacteristicValues() {
  }

  public static @NonNull CharacteristicValue<SpecificGravityValue> asSpecificGravity(
      @NonNull CharacteristicValue<SugarConcentration> sugar
  ) {
    requireNonNull(sugar, "sugar characteristic cannot be null");
    return switch (sugar) {
      case PointCharacteristicValue<SugarConcentration>(var point) ->
          PointCharacteristicValue.of(point.specificGravity());
      case IntervalCharacteristicValue<SugarConcentration>(var lower, var upper) ->
          IntervalCharacteristicValue.of(lower.specificGravity(), upper.specificGravity());
    };
  }
}
