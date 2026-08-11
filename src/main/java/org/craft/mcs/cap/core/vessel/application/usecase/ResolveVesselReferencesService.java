package org.craft.mcs.cap.core.vessel.application.usecase;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.vessel.application.authorization.port.out.VesselAccessControl;
import org.craft.mcs.cap.core.vessel.application.port.in.ResolveVesselReferencesQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ResolveVesselReferencesUseCase;
import org.craft.mcs.cap.core.vessel.application.port.out.VesselCatalogQuery;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Bulk resolver used by ACL adapters to enrich batch read models without catalog table joins. */
@Component
@RequiredArgsConstructor
public final class ResolveVesselReferencesService implements ResolveVesselReferencesUseCase {

  private final @NonNull VesselCatalogQuery vesselCatalogQuery;
  private final @NonNull VesselAccessControl vesselAccessControl;

  @Override
  public @NonNull Map<@NonNull VesselId, @NonNull VesselReferenceView> resolve(
      @NonNull ResolveVesselReferencesQuery query
  ) {
    requireNonNull(query, "resolve vessel references query cannot be null");
    vesselAccessControl.requireReadAccess(query.workspaceId());
    return Map.copyOf(vesselCatalogQuery.findReferences(query.workspaceId(), query.vesselIds()));
  }
}
