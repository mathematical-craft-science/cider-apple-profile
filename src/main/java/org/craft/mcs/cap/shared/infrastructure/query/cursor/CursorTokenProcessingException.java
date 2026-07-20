package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import java.io.Serial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Indicates an internal failure while generating or verifying an opaque cursor token. */
public final class CursorTokenProcessingException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public CursorTokenProcessingException(
      @NonNull String message,
      @Nullable Throwable cause
  ) {
    super(message, cause);
  }
}
