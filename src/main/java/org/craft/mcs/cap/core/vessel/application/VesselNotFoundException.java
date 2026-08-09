package org.craft.mcs.cap.core.vessel.application;

import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Raised when a requested vessel does not exist. */
@SuppressWarnings("java:S110")
public final class VesselNotFoundException extends ApplicationNotFoundException {

  public VesselNotFoundException(@NonNull VesselId vesselId) {
    super("vessel not found: " + requireNonNull(vesselId, "vessel id cannot be null").value());
  }
}
