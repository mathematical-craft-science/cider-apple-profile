package org.craft.mcs.cap.core.formulation.calculation.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Published failure for a composition request that violates calculation rules. */
public final class InvalidCompositionRequestException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidCompositionRequestException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
