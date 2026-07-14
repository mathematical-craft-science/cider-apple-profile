package org.craft.mcs.cap.shared.kernel;

import java.io.Serial;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception for operations that conflict with the current aggregate state.
 *
 * <p>Use this when the command is structurally valid, but cannot be applied to
 * the current domain state, for example because available stock changed.
 */
public final class DomainConflictException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public DomainConflictException(@NonNull String message) {
    super(message);
  }
}
