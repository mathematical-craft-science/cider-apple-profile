package org.craft.mcs.cap.core.cideranalysis.composition.optimization.domain;

import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception for invalid chemical profile composition optimization requests or settings.
 */
public final class InvalidChemicalProfileCompositionOptimizationException extends DomainException {

  @java.io.Serial
  private static final long serialVersionUID = 1L;

  public InvalidChemicalProfileCompositionOptimizationException(@NonNull String message) {
    super(message);
  }
}
