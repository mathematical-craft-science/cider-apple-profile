package org.craft.mcs.cap.core.vessel.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.VesselNotFoundException;
import org.craft.mcs.cap.core.vessel.application.VesselOwnershipMismatchException;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageView;
import org.craft.mcs.cap.core.vessel.application.port.in.GetVesselQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.GetVesselUseCase;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCatalogQuery;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCurrentContentQuery;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads one Vessel while preserving workspace ownership semantics. */
@Component
@RequiredArgsConstructor
public final class GetVesselService implements GetVesselUseCase {

  private final @NonNull VesselCatalogQuery vesselCatalogQuery;
  private final @NonNull VesselCurrentContentQuery vesselCurrentContentQuery;
  private final @NonNull VesselAccessControl vesselAccessControl;

  @Override
  public @NonNull VesselUsageView get(@NonNull GetVesselQuery query) {
    requireNonNull(query, "get vessel query cannot be null");
    vesselAccessControl.requireReadAccess(query.workspaceId());
    final var vessel = vesselCatalogQuery.findById(query.vesselId())
        .orElseThrow(() -> new VesselNotFoundException(query.vesselId()));
    if (!vessel.workspaceId().equals(query.workspaceId())) {
      throw new VesselOwnershipMismatchException(
          vessel.vesselId(),
          query.workspaceId(),
          vessel.workspaceId()
      );
    }
    return new VesselUsageView(
        vessel,
        vesselCurrentContentQuery.findByVesselId(vessel.vesselId()).orElse(null) // NOSONAR
    );
  }
}
