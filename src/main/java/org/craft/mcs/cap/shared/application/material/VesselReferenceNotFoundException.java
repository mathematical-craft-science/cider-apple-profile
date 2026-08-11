package org.craft.mcs.cap.shared.application.material;

import org.craft.mcs.cap.shared.application.ApplicationNotFoundException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Indicates a broken material-to-vessel reference while building a cross-context read model. */
@SuppressWarnings("java:S110")
public final class VesselReferenceNotFoundException extends ApplicationNotFoundException {

  public VesselReferenceNotFoundException(@NonNull VesselId vesselId) {
    super("referenced vessel not found: " + requireNonNull(vesselId, "vessel id cannot be null").value());
  }
}
