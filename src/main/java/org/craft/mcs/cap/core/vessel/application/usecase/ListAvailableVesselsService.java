package org.craft.mcs.cap.core.vessel.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ListAvailableVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselAvailabilityQuery;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Lists currently selectable vessels while leaving assignment validation authoritative at write time. */
@Component
@RequiredArgsConstructor
public final class ListAvailableVesselsService implements ListAvailableVesselsUseCase {

  private final @NonNull VesselAvailabilityQuery vesselAvailabilityQuery;
  private final @NonNull VesselAccessControl vesselAccessControl;

  @Override
  public @NonNull List<@NonNull VesselUsageView> listAvailable(
      @NonNull ListAvailableVesselsQuery query
  ) {
    requireNonNull(query, "list available vessels query cannot be null");
    vesselAccessControl.requireReadAccess(query.workspaceId());
    return vesselAvailabilityQuery.findAvailable(query.workspaceId(), query.nameContains()).stream()
        .map(VesselUsageView::empty)
        .toList();
  }
}
