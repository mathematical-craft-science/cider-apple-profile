package org.craft.mcs.cap.core.vessel.application.port.in;

import java.util.Set;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Bulk query for display references to workspace-owned vessels. */
public record ResolveVesselReferencesQuery(
    @NonNull WorkspaceId workspaceId,
    @NonNull Set<@NonNull VesselId> vesselIds
) {

  public ResolveVesselReferencesQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    vesselIds = Set.copyOf(requireNonNull(vesselIds, "vessel ids cannot be null"));
  }
}
