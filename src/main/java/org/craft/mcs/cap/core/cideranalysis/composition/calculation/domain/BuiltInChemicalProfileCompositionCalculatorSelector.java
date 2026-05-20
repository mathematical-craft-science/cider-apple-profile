package org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain;

import java.util.EnumMap;
import java.util.Map;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Selects one of the built-in chemical profile composition calculation strategies.
 *
 * <p>Currently only {@link ChemicalProfileCompositionCalculatorCode#MASS_BALANCE} is registered.
 * Future calculators should be added here when they are part of the built-in domain catalog, not
 * wired directly into application services.
 */
public final class BuiltInChemicalProfileCompositionCalculatorSelector
    implements ChemicalProfileCompositionCalculatorSelector {

  private final @NonNull Map<@NonNull ChemicalProfileCompositionCalculatorCode, @NonNull ChemicalProfileCompositionCalculator> calculators;

  public BuiltInChemicalProfileCompositionCalculatorSelector() {
    final var builtInCalculators = new EnumMap<ChemicalProfileCompositionCalculatorCode, ChemicalProfileCompositionCalculator>(
        ChemicalProfileCompositionCalculatorCode.class
    );
    builtInCalculators.put(
        ChemicalProfileCompositionCalculatorCode.MASS_BALANCE,
        new MassBalanceChemicalProfileCompositionStrategy()
    );
    this.calculators = Map.copyOf(builtInCalculators);
  }

  @Override
  public @NonNull ChemicalProfileCompositionCalculator select(
      @NonNull ChemicalProfileCompositionCalculatorCode code
  ) {
    requireNonNull(code, "chemical profile composition calculator code cannot be null");
    final var calculator = calculators.get(code);
    if (calculator == null) { // NOSONAR
      throw new UnsupportedChemicalProfileCompositionCalculatorException(code);
    }
    return calculator;
  }
}
