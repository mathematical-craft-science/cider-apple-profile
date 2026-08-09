package org.craft.mcs.cap.core.feedstock.application.vessel.port.out;

import java.util.Map;
import java.util.Set;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Feedstock-owned ACL port for resolving vessel display references in bulk. */
public interface FeedstockVesselReferenceQuery {

  @NonNull Map<@NonNull VesselId, @NonNull VesselReferenceView> resolveReferences(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull VesselId> vesselIds
  );
}
