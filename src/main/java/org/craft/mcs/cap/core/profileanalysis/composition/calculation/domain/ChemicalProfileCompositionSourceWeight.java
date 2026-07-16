package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


/**
 * Weight assigned to a composition source.
 *
 * <p>Implementations represent either an absolute volume or a relative proportion. Calculators use
 * {@link #value()} as the numeric weight and validate that one calculation does not mix modes.
 */
public sealed interface ChemicalProfileCompositionSourceWeight
    permits AbsoluteCompositionVolume, CompositionProportion {

  @NonNull
  BigDecimal value();
}
