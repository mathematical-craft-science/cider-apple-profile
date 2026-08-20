package org.craft.mcs.cap.core.appleprofile.analysis.application.profiletarget;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.NonNull;


/** Apple analysis failure when its requested target cannot be evaluated. */
public final class AppleTargetFitUnavailableException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public AppleTargetFitUnavailableException(
      @NonNull String message,
      @NonNull Throwable cause
  ) {
    super(message, cause);
  }
}
