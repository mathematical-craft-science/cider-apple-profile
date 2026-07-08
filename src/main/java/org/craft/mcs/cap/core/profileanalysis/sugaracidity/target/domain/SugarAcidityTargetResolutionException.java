package org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain;


import java.io.Serial;
import org.craft.mcs.cap.shared.kernel.DomainException;
import org.jspecify.annotations.NonNull;


public final class SugarAcidityTargetResolutionException extends DomainException {

  @Serial
  private static final long serialVersionUID = 1L;

  public SugarAcidityTargetResolutionException(@NonNull String message) {
    super(message);
  }
}
