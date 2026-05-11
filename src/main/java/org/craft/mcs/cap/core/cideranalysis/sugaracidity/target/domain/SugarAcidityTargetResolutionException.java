package org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain;


import java.io.Serial;


public final class SugarAcidityTargetResolutionException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = 1L;

  public SugarAcidityTargetResolutionException(String message) {
    super(message);
  }
}
