package org.craft.mcs.cap.core.formulation.optimization.domain;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception for invalid composition optimization requests or settings.
 */
public final class InvalidCompositionOptimizationException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public InvalidCompositionOptimizationException(@NonNull String message) {
    super(message);
  }
}
