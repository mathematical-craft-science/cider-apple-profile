package org.craft.mcs.cap.core.cideranalysis.targetfit.domain.scoring;

import org.craft.mcs.cap.core.cideranalysis.graph.domain.geometry.SugarAcidityGraphPoint;
import org.craft.mcs.cap.shared.kernel.characteristic.CharacteristicValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.shared.kernel.measurement.MeasurementValueMath.midpoint;


/**
 * Declares how interval characteristics become one representative point for target scoring.
 *
 * <p>Target-area intersection can be added as another policy without changing graph projection or
 * classification semantics.</p>
 */
public enum ProfileTargetIntervalPolicy {

  /** Preserves existing target-scoring behavior by using the exact interval midpoint. */
  MIDPOINT;

  public @NonNull ProfileTargetScoringPoint representativePoint(
      @NonNull ProfileTargetScoringInput input
  ) {
    requireNonNull(input, "profile target scoring input cannot be null");
    return new ProfileTargetScoringPoint(
        new SugarAcidityGraphPoint(
            specificGravity(input.specificGravity()),
            titratableAcidity(input.titratableAcidity())
        ),
        input.tannin() == null ? null : tannin(input.tannin()) // NOSONAR
    );
  }

  private static @NonNull SpecificGravityValue specificGravity(
      @NonNull CharacteristicValue<SpecificGravityValue> value
  ) {
    return SpecificGravityValue.of(midpoint(value));
  }

  private static @NonNull TitratableAcidityValue titratableAcidity(
      @NonNull CharacteristicValue<TitratableAcidityValue> value
  ) {
    return TitratableAcidityValue.of(midpoint(value));
  }

  private static @NonNull TanninValue tannin(
      @NonNull CharacteristicValue<TanninValue> value
  ) {
    return TanninValue.of(midpoint(value));
  }
}
