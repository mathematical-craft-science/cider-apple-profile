package org.craft.mcs.cap.shared.kernel;

import java.io.Serial;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Base exception for domain-level failures.
 *
 * <p>Domain code should throw subclasses of this type instead of technical
 * exceptions such as {@link IllegalArgumentException}. Application and transport
 * layers can map it to stable API errors without depending on implementation
 * details of domain validation.
 */
public abstract class DomainException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  protected DomainException(@NonNull String message) {
    super(message);
  }

  protected DomainException(
      @NonNull String message,
      @Nullable Throwable cause
  ) {
    super(message, cause);
  }
}
