package org.craft.mcs.cap.core.production.batch.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.application.vessel.port.out.ProductionVesselReferenceQuery;
import org.craft.mcs.cap.shared.application.material.VesselReferenceNotFoundException;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves Vessel display references for production read models through a bulk ACL call. */
@Component
@RequiredArgsConstructor
public final class ProductionVesselReferenceResolver {

  private final @NonNull ProductionVesselReferenceQuery vesselReferenceQuery;

  public @NonNull VesselReferenceView resolve(@NonNull ProductionBatchView batch) {
    requireNonNull(batch, "production batch view cannot be null");
    return requireNonNull(
        resolveAll(batch.workspaceId(), List.of(batch)).get(batch.vesselId()),
        "resolved production vessel reference cannot be null"
    );
  }

  public @NonNull Map<@NonNull VesselId, @NonNull VesselReferenceView> resolveAll(
      @NonNull WorkspaceId workspaceId,
      @NonNull List<@NonNull ProductionBatchView> batches
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    final Set<VesselId> vesselIds = List.copyOf(requireNonNull(batches, "production batches cannot be null"))
        .stream()
        .map(ProductionBatchView::vesselId)
        .collect(Collectors.toUnmodifiableSet());
    final var vessels = Map.copyOf(vesselReferenceQuery.resolveReferences(workspaceId, vesselIds));
    for (final var vesselId : vesselIds) {
      if (!vessels.containsKey(vesselId)) {
        throw new VesselReferenceNotFoundException(vesselId);
      }
    }
    return vessels;
  }
}
