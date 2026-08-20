package org.craft.mcs.cap.core.production.planning.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Production Planning failure when the selected candidate target cannot be evaluated. */
public final class ProductionCandidateTargetFitUnavailableException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ProductionCandidateTargetFitUnavailableException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
