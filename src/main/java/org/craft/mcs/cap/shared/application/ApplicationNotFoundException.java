package org.craft.mcs.cap.shared.application;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/**
 * Application exception for use-case resources that cannot be found.
 */
public abstract class ApplicationNotFoundException extends ApplicationException {

  @Serial
  private static final long serialVersionUID = 1L;

  protected ApplicationNotFoundException(@NonNull String message) {
    super(message);
  }
}
