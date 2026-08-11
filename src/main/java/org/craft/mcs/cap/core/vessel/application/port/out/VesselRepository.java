package org.craft.mcs.cap.core.vessel.application.port.out;

import java.util.Optional;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Aggregate persistence port preserving the authoritative persisted Vessel revision. */
public interface VesselRepository {

  @NonNull VersionedAggregate<Vessel> create(@NonNull Vessel vessel);

  @NonNull VersionedAggregate<Vessel> update(@NonNull VersionedAggregate<Vessel> vessel);

  @NonNull Optional<@NonNull VersionedAggregate<Vessel>> findVersionedById(@NonNull VesselId vesselId);

  /** Locks one vessel row for the duration of the caller's existing transaction. */
  @NonNull Optional<@NonNull VersionedAggregate<Vessel>> lockVersionedById(@NonNull VesselId vesselId);

  boolean existsByWorkspaceAndNameExcluding(
      @NonNull WorkspaceId workspaceId,
      @NonNull String name,
      @NonNull VesselId excludedVesselId
  );
}
