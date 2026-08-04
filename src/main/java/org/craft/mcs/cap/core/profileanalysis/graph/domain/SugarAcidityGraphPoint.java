package org.craft.mcs.cap.core.profileanalysis.graph.domain;

import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Two-dimensional coordinate in the SG/TA graph space.
 *
 * <p>The same point type is used by graph projection, zone assessment, and target geometry because
 * all three operate in one coordinate system.</p>
 */
@ValueObject
public record SugarAcidityGraphPoint(
    @NonNull SpecificGravityValue specificGravity,
    @NonNull TitratableAcidityValue titratableAcidity
) {

  public SugarAcidityGraphPoint {
    requireNonNull(specificGravity, "graph specific gravity coordinate cannot be null");
    requireNonNull(titratableAcidity, "graph titratable acidity coordinate cannot be null");
  }
}
