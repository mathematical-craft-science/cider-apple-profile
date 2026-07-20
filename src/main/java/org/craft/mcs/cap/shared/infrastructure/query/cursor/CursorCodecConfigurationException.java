package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import java.io.Serial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Indicates invalid signing-key or lifetime configuration for the cursor codec. */
public final class CursorCodecConfigurationException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public CursorCodecConfigurationException(@NonNull String message) {
    super(message);
  }

  public CursorCodecConfigurationException(
      @NonNull String message,
      @Nullable Throwable cause
  ) {
    super(message, cause);
  }
}
