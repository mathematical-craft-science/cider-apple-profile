package org.craft.mcs.cap.core.vessel.application.usecase;

import org.craft.mcs.cap.core.vessel.application.contract.VesselView;
import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps Vessel aggregate state to immutable application projections. */
final class VesselViewMapper {

  private VesselViewMapper() {
  }

  static @NonNull VesselView toView(@NonNull VersionedAggregate<Vessel> state) {
    requireNonNull(state, "versioned vessel cannot be null");
    return toView(state.aggregate(), state.revision());
  }

  static @NonNull VesselView toView(
      @NonNull Vessel vessel,
      @NonNull AggregateRevision revision
  ) {
    requireNonNull(vessel, "vessel cannot be null");
    return new VesselView(
        vessel.vesselId(),
        vessel.workspaceId(),
        vessel.name(),
        vessel.nominalCapacity(),
        vessel.material(),
        vessel.operationalStatus(),
        revision
    );
  }

  static @NonNull VesselReferenceView toReference(@NonNull Vessel vessel) {
    requireNonNull(vessel, "vessel cannot be null");
    return new VesselReferenceView(vessel.vesselId(), vessel.name());
  }
}
