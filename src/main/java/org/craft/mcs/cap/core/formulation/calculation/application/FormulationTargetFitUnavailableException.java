package org.craft.mcs.cap.core.formulation.calculation.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Formulation failure when the requested target cannot be evaluated. */
public final class FormulationTargetFitUnavailableException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public FormulationTargetFitUnavailableException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
