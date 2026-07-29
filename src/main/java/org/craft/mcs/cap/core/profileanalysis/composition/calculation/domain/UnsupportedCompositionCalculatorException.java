package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception raised when no composition calculator exists for a requested code.
 */
public final class UnsupportedCompositionCalculatorException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedCompositionCalculatorException(
      @NonNull CompositionCalculatorCode code
  ) {
    super("No composition calculator registered for code: " + code);
  }
}
