package org.craft.mcs.cap.core.formulation.optimization.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Published failure for an optimization request that violates formulation rules. */
public final class InvalidCompositionOptimizationRequestException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidCompositionOptimizationRequestException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
