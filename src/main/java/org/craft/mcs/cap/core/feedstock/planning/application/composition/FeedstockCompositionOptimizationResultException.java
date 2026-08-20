package org.craft.mcs.cap.core.feedstock.planning.application.composition;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationDataIntegrityException;
import org.jspecify.annotations.NonNull;


/** Internal failure raised when Formulation returns a source outside the requested Feedstock problem. */
@SuppressWarnings("java:S110")
public final class FeedstockCompositionOptimizationResultException extends ApplicationDataIntegrityException {

  @Serial
  private static final long serialVersionUID = 1L;

  public FeedstockCompositionOptimizationResultException(@NonNull String message) {
    super(message);
  }

  public FeedstockCompositionOptimizationResultException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
