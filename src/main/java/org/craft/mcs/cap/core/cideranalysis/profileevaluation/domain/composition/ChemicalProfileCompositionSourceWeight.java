package org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;


public sealed interface ChemicalProfileCompositionSourceWeight permits AbsoluteCompositionVolume, CompositionProportion {

  @NonNull
  BigDecimal value();
}
