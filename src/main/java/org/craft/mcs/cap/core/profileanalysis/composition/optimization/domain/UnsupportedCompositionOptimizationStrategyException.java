package org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain;

import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception raised when no optimization strategy exists for a requested algorithm.
 */
public final class UnsupportedCompositionOptimizationStrategyException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public UnsupportedCompositionOptimizationStrategyException(
      @NonNull CompositionOptimizationAlgorithm algorithm
  ) {
    super("No composition optimization strategy registered for algorithm: " + algorithm);
  }
}
