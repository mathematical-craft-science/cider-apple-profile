package org.craft.mcs.cap.shared.application;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/**
 * Application exception for valid use-case requests that cannot be processed
 * with the currently available application data.
 */
public abstract class ApplicationUnprocessableException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  protected ApplicationUnprocessableException(@NonNull String message) {
    super(message);
  }
}
