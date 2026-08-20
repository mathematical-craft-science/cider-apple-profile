package org.craft.mcs.cap.core.vessel.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.port.in.ListVesselsQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ListVesselsUseCase;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCatalogQuery;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Lists the vessel catalog visible in one workspace. */
@Component
@RequiredArgsConstructor
public final class ListVesselsService implements ListVesselsUseCase {

  private final @NonNull VesselCatalogQuery vesselCatalogQuery;
  private final @NonNull VesselCurrentContentQuery vesselCurrentContentQuery;
  private final @NonNull VesselAccessControl vesselAccessControl;

  @Override
  public @NonNull List<@NonNull VesselUsageView> list(@NonNull ListVesselsQuery query) {
    requireNonNull(query, "list vessels query cannot be null");
    vesselAccessControl.requireReadAccess(query.workspaceId());
    final var currentContents = vesselCurrentContentQuery.findAllByWorkspace(query.workspaceId());
    return vesselCatalogQuery.findAllByWorkspace(query.workspaceId()).stream()
        .map(vessel -> new VesselUsageView(vessel, currentContents.get(vessel.vesselId())))
        .toList();
  }
}
