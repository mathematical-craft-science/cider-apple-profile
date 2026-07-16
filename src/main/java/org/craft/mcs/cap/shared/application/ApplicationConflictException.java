package org.craft.mcs.cap.shared.application;

import java.io.Serial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Application exception for use-case conflicts caused by stale or incompatible current state.
 */
public abstract class ApplicationConflictException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  protected ApplicationConflictException(@NonNull String message) {
    super(message);
  }

  protected ApplicationConflictException(
      @NonNull String message,
      @Nullable Throwable cause
  ) {
    super(message, cause);
  }
}
