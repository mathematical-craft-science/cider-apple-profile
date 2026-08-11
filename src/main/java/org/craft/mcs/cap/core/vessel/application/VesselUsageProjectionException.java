package org.craft.mcs.cap.core.vessel.application;

import org.craft.mcs.cap.shared.application.ApplicationDataIntegrityException;
import org.jspecify.annotations.NonNull;


/** Raised when persisted material assignments cannot form a valid single-content vessel view. */
@SuppressWarnings("java:S110")
public final class VesselUsageProjectionException extends ApplicationDataIntegrityException {

  public VesselUsageProjectionException(@NonNull String message) {
    super(message);
  }
}
