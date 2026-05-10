package org.craft.mcs.cap.shared.application.query;

import java.io.Serial;
import org.jspecify.annotations.Nullable;


public final class MalformedCursorException extends IllegalArgumentException {

  @Serial
  private static final long serialVersionUID = 1L;

  public MalformedCursorException() {
    super("cursor is malformed");
  }

  public MalformedCursorException(@Nullable Throwable cause) {
    super("cursor is malformed", cause);
  }
}
