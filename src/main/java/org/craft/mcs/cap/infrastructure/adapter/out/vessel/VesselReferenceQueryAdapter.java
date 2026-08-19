package org.craft.mcs.cap.infrastructure.adapter.out.vessel;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.vessel.port.out.FeedstockVesselReferenceQuery;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselReferenceQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ResolveVesselReferencesQuery;
import org.craft.mcs.cap.core.vessel.application.port.in.ResolveVesselReferencesUseCase;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/** ACL adapter exposing Vessel display-reference queries to material-owning contexts. */
@Component
@RequiredArgsConstructor
public final class VesselReferenceQueryAdapter
    implements FeedstockVesselReferenceQuery, ProductionVesselReferenceQuery {

  private final @NonNull ResolveVesselReferencesUseCase resolveVesselReferencesUseCase;

  @Override
  public @NonNull Map<@NonNull VesselId, @NonNull VesselReferenceView> resolveReferences(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull VesselId> vesselIds
  ) {
    return resolveVesselReferencesUseCase.resolve(new ResolveVesselReferencesQuery(
        workspaceId,
        vesselIds
    ));
  }
}
