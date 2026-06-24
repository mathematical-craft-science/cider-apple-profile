package org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain;

import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


/**
 * Domain exception for invalid fixed chemical profile composition requests.
 */
public final class InvalidChemicalProfileCompositionException extends DomainException {

  @java.io.Serial
  private static final long serialVersionUID = 1L;

  public InvalidChemicalProfileCompositionException(@NonNull String message) {
    super(message);
  }
}
