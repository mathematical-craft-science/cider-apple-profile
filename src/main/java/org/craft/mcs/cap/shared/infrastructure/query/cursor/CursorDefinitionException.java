package org.craft.mcs.cap.shared.infrastructure.query.cursor;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/** Indicates an invalid infrastructure cursor scope or token contract definition. */
public final class CursorDefinitionException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public CursorDefinitionException(@NonNull String message) {
    super(message);
  }
}
