package org.craft.mcs.cap.shared.kernel.measurement.unit;

import javax.measure.Unit;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Supported reference acid bases used to express titratable acidity measurements. */
public enum TitratableAcidityBasis {
  /** Titratable acidity expressed as equivalent grams of malic acid per liter. */
  MALIC_ACID(BeverageUnits.GRAM_PER_LITER_AS_MALIC_ACID),

  /** Titratable acidity expressed as equivalent grams of lactic acid per liter. */
  LACTIC_ACID(BeverageUnits.GRAM_PER_LITRE_AS_LACTIC_ACID),

  /** Titratable acidity expressed as equivalent grams of citric acid per liter. */
  CITRIC_ACID(BeverageUnits.GRAM_PER_LITRE_AS_CITRIC_ACID);

  private final @NonNull Unit<TitratableAcidity> unit;

  TitratableAcidityBasis(@NonNull Unit<TitratableAcidity> unit) {
    this.unit = requireNonNull(unit, "titratable acidity unit cannot be null");
  }

  /** Returns the JSR-385 unit corresponding to this acidity basis. */
  public @NonNull Unit<TitratableAcidity> unit() {
    return unit;
  }
}
