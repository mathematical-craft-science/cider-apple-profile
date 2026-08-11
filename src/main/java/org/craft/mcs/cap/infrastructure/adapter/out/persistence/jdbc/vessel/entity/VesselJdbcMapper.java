package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.vessel.entity;

import org.craft.mcs.cap.core.vessel.domain.model.Vessel;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Maps the Vessel aggregate to and from its Spring Data JDBC row. */
public final class VesselJdbcMapper {

  private VesselJdbcMapper() {
  }

  public static @NonNull VesselJdbcEntity fromDomain(@NonNull Vessel vessel) {
    requireNonNull(vessel, "vessel cannot be null");
    return new VesselJdbcEntity(
        vessel.vesselId().value(),
        vessel.workspaceId().value(),
        vessel.name(),
        vessel.nominalCapacity().liters(),
        vessel.material(),
        vessel.operationalStatus()
    );
  }

  public static @NonNull Vessel toDomain(@NonNull VesselJdbcEntity entity) {
    requireNonNull(entity, "vessel row cannot be null");
    return Vessel.restore(
        vesselId(entity),
        workspaceId(entity),
        requireNonNull(entity.name, "vessel name cannot be null"),
        VolumeValue.ofLiters(requireNonNull(
            entity.nominalCapacityLiters,
            "vessel nominal capacity cannot be null"
        )),
        requireNonNull(entity.material, "vessel material cannot be null"),
        requireNonNull(entity.operationalStatus, "vessel operational status cannot be null")
    );
  }

  public static @NonNull VersionedAggregate<Vessel> toVersionedAggregate(
      @NonNull VesselJdbcEntity entity
  ) {
    return new VersionedAggregate<>(toDomain(entity), AggregateRevision.of(entity.version));
  }

  private static @NonNull VesselId vesselId(@NonNull VesselJdbcEntity entity) {
    return VesselId.of(requireNonNull(entity.vesselId, "vessel id cannot be null"));
  }

  private static @NonNull WorkspaceId workspaceId(@NonNull VesselJdbcEntity entity) {
    return WorkspaceId.of(requireNonNull(entity.workspaceId, "workspace id cannot be null"));
  }
}
