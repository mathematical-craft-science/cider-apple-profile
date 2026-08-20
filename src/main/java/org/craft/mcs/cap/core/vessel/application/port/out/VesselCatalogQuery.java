package org.craft.mcs.cap.core.vessel.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.vessel.application.contract.VesselView;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read-model query port for catalog and cross-context vessel references. */
public interface VesselCatalogQuery {

  @NonNull Optional<@NonNull VesselView> findById(@NonNull VesselId vesselId);

  @NonNull List<@NonNull VesselView> findAllByWorkspace(@NonNull WorkspaceId workspaceId);

  @NonNull Map<@NonNull VesselId, @NonNull VesselReferenceView> findReferences(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull VesselId> vesselIds
  );
}
