package org.craft.mcs.cap.core.production.planning.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Production Planning failure for candidate characteristics rejected by Formulation. */
public final class InvalidProductionCandidateCalculationException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidProductionCandidateCalculationException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
