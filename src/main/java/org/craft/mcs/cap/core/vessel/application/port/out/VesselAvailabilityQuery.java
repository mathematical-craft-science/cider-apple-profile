package org.craft.mcs.cap.core.vessel.application.port.out;

import java.util.List;
import org.craft.mcs.cap.core.vessel.application.model.VesselView;
import org.craft.mcs.cap.shared.application.query.TextSearchCriteria;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Read port selecting operational vessels without an active material assignment. */
public interface VesselAvailabilityQuery {

  @NonNull List<@NonNull VesselView> findAvailable(
      @NonNull WorkspaceId workspaceId,
      @Nullable TextSearchCriteria nameContains
  );
}
