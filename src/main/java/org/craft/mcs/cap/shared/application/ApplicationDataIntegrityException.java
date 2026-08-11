package org.craft.mcs.cap.shared.application;

import java.io.Serial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Application failure raised when persisted state cannot form a valid application read model.
 *
 * <p>This is an internal integrity failure rather than a conflict caused by the caller's request.
 */
public abstract class ApplicationDataIntegrityException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  protected ApplicationDataIntegrityException(@NonNull String message) {
    super(message);
  }

  protected ApplicationDataIntegrityException(
      @NonNull String message,
      @Nullable Throwable cause
  ) {
    super(message, cause);
  }
}
