package org.craft.mcs.cap.shared.application;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/**
 * Application exception for invalid command or query parameters accepted by a use case boundary.
 */
public final class ApplicationValidationException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  public ApplicationValidationException(@NonNull String message) {
    super(message);
  }
}
