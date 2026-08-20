package org.craft.mcs.cap.core.feedstock.planning.application.composition;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Feedstock Planning failure for an optimization request rejected by formulation rules. */
public final class InvalidFeedstockCompositionOptimizationException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidFeedstockCompositionOptimizationException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
