package org.craft.mcs.cap.core.production.batch.domain.model.value;

import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Immutable point-in-time snapshot of the calculated profile accepted for a ProductionBatch.
 *
 * <p>The snapshot contains canonical point values established during candidate calculation. It is
 * not measurement evidence and does not represent the batch's evolving current measurement
 * state.</p>
 */
@ValueObject
public record ProductionProfileSnapshot(
    @NonNull SpecificGravityValue specificGravity,
    @NonNull TitratableAcidityValue titratableAcidity,
    @Nullable TanninValue tannin,
    @Nullable YeastAssimilableNitrogenValue yeastAssimilableNitrogen
) {

  public ProductionProfileSnapshot {
    requireNonNull(specificGravity, "production specific gravity cannot be null");
    requireNonNull(titratableAcidity, "production titratable acidity cannot be null");
  }

  public ProductionProfileSnapshot(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity,
      @Nullable TanninValue tannin
  ) {
    this(specificGravity, titratableAcidity, tannin, null);
  }
}
