package org.craft.mcs.cap.shared.application.query;

import java.io.Serial;
import org.craft.mcs.cap.shared.application.ApplicationException;
import org.jspecify.annotations.Nullable;


/** Application query failure raised when a pagination cursor cannot be trusted or decoded. */
public final class MalformedCursorException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public MalformedCursorException() {
    super("cursor is malformed");
  }

  public MalformedCursorException(@Nullable Throwable cause) {
    super("cursor is malformed", cause);
  }
}
