package org.craft.mcs.cap.core.formulation.calculation.domain;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception for invalid fixed composition requests.
 */
public final class InvalidCompositionException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidCompositionException(@NonNull String message) {
    super(message);
  }
}
