package org.craft.mcs.cap.core.vessel.application.port.out;

import java.util.Map;
import java.util.Optional;
import org.craft.mcs.cap.core.vessel.application.contract.VesselCurrentContentView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read port projecting current active material assignments without owning material lifecycle. */
public interface VesselCurrentContentQuery {

  @NonNull Optional<@NonNull VesselCurrentContentView> findByVesselId(@NonNull VesselId vesselId);

  @NonNull Map<@NonNull VesselId, @NonNull VesselCurrentContentView> findAllByWorkspace(
      @NonNull WorkspaceId workspaceId
  );
}
