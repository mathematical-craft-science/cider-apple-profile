package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Query for one workspace-owned vessel. */
public record GetVesselQuery(
    @NonNull VesselId vesselId,
    @NonNull WorkspaceId workspaceId
) {

  public GetVesselQuery {
    requireNonNull(vesselId, "vessel id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
  }
}
