package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import java.math.BigDecimal;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record CompositionProportion(@NonNull BigDecimal value) implements ChemicalProfileCompositionSourceWeight {

  public CompositionProportion {
    requireNonNull(value, "composition proportion cannot be null");

    if (value.signum() <= 0) {
      throw new IllegalArgumentException("composition proportion must be greater than zero");
    }
  }

  public static @NonNull CompositionProportion of(@NonNull String value) {
    return new CompositionProportion(new BigDecimal(value));
  }
}
