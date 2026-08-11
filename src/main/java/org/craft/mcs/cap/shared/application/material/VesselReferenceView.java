package org.craft.mcs.cap.shared.application.material;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Minimal published reference used to identify and display a Vessel across bounded contexts. */
public record VesselReferenceView(
    @NonNull VesselId vesselId,
    @NonNull String name
) {

  public VesselReferenceView {
    requireNonNull(vesselId, "vessel id cannot be null");
    name = requireNonNull(name, "vessel name cannot be null").strip();
    if (name.isBlank()) {
      throw new ApplicationValidationException("vessel reference name cannot be blank");
    }
  }
}
