package org.craft.mcs.cap.core.cideranalysis.targetfit.application;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Published failure when a requested target cannot be resolved to configured scoring geometry. */
public final class TargetFitTargetUnavailableException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public TargetFitTargetUnavailableException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
