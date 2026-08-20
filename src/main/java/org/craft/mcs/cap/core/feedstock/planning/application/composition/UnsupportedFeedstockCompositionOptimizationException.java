package org.craft.mcs.cap.core.feedstock.planning.application.composition;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Feedstock Planning failure for an optimization strategy unavailable in Formulation. */
public final class UnsupportedFeedstockCompositionOptimizationException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedFeedstockCompositionOptimizationException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
