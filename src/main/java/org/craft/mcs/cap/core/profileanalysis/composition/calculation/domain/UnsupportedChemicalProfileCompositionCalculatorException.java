package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception raised when no composition calculator exists for a requested code.
 */
public final class UnsupportedChemicalProfileCompositionCalculatorException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedChemicalProfileCompositionCalculatorException(
      @NonNull ChemicalProfileCompositionCalculatorCode code
  ) {
    super("No chemical profile composition calculator registered for code: " + code);
  }
}
