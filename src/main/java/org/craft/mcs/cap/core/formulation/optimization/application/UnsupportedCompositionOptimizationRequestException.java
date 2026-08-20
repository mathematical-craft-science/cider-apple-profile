package org.craft.mcs.cap.core.formulation.optimization.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Published failure for an optimization algorithm unavailable to the use case. */
public final class UnsupportedCompositionOptimizationRequestException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedCompositionOptimizationRequestException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
