package org.craft.mcs.cap.shared.application;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/**
 * Application exception for use-case actions forbidden by ownership or access rules.
 */
public abstract class ApplicationForbiddenException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  protected ApplicationForbiddenException(@NonNull String message) {
    super(message);
  }
}
