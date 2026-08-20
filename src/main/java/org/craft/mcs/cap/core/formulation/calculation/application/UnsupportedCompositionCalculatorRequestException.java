package org.craft.mcs.cap.core.formulation.calculation.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Published failure for a calculator code unavailable to the calculation use case. */
public final class UnsupportedCompositionCalculatorRequestException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedCompositionCalculatorRequestException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
