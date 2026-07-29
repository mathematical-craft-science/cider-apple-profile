package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Selects one of the built-in composition calculation strategies.
 *
 * <p>Currently only {@link CompositionCalculatorCode#MASS_BALANCE} is registered.
 * Future calculators should be added here when they are part of the built-in domain catalog, not
 * wired directly into application services.
 */
public final class BuiltInCompositionCalculatorSelector
    implements CompositionCalculatorSelector {

  private final @NonNull Map<
      @NonNull CompositionCalculatorCode,
      @NonNull CompositionCalculator
      > calculators;

  public BuiltInCompositionCalculatorSelector() {
    final var builtInCalculators = new EnumMap<
        CompositionCalculatorCode,
        CompositionCalculator
        >(
        CompositionCalculatorCode.class
    );
    builtInCalculators.put(
        CompositionCalculatorCode.MASS_BALANCE,
        new MassBalanceCompositionCalculator()
    );
    this.calculators = Map.copyOf(builtInCalculators);
  }

  @Override
  public @NonNull CompositionCalculator select(
      @NonNull CompositionCalculatorCode code
  ) {
    requireNonNull(code, "composition calculator code cannot be null");
    return Optional.ofNullable(calculators.get(code))
        .orElseThrow(() -> new UnsupportedCompositionCalculatorException(code));
  }
}
