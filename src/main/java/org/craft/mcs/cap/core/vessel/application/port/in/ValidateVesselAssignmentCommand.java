package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Command to lock and validate a vessel within a material-batch creation transaction. */
public record ValidateVesselAssignmentCommand(
    @NonNull VesselId vesselId,
    @NonNull WorkspaceId workspaceId,
    @NonNull VolumeValue materialVolume
) {

  public ValidateVesselAssignmentCommand {
    requireNonNull(vesselId, "vessel id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(materialVolume, "material volume cannot be null");
  }
}
