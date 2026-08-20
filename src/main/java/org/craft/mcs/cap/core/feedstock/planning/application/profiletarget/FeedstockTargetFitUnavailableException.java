package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Feedstock Planning failure when its requested target cannot be evaluated. */
public final class FeedstockTargetFitUnavailableException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public FeedstockTargetFitUnavailableException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
