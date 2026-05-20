package org.craft.mcs.cap.shared.application;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Base exception for application/use-case failures.
 *
 * <p>Application exceptions describe use-case outcomes such as missing resources,
 * ownership mismatches, or insufficient source data. They are distinct from
 * domain exceptions, which protect aggregate and value object invariants.
 */
public abstract class ApplicationException extends RuntimeException {

  @java.io.Serial
  private static final long serialVersionUID = 1L;

  protected ApplicationException(@NonNull String message) {
    super(message);
  }

  protected ApplicationException(@NonNull String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}
