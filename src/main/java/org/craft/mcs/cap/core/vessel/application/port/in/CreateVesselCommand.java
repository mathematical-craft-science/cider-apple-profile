package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Command for registering a physical vessel in a workspace catalog. */
public record CreateVesselCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull String name,
    @NonNull VolumeValue nominalCapacity,
    @NonNull VesselMaterial material
) {

  public CreateVesselCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(name, "vessel name cannot be null");
    requireNonNull(nominalCapacity, "vessel nominal capacity cannot be null");
    requireNonNull(material, "vessel material cannot be null");
  }
}
