package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.core.vessel.domain.model.VesselMaterial;
import org.craft.mcs.cap.core.vessel.domain.model.VesselOperationalStatus;
import org.craft.mcs.cap.shared.application.persistence.AggregateRevision;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Revision-checked replacement of editable vessel catalog data. */
public record UpdateVesselCommand(
    @NonNull VesselId vesselId,
    @NonNull WorkspaceId workspaceId,
    @NonNull AggregateRevision expectedRevision,
    @NonNull String name,
    @NonNull VolumeValue nominalCapacity,
    @NonNull VesselMaterial material,
    @NonNull VesselOperationalStatus operationalStatus
) {

  public UpdateVesselCommand {
    requireNonNull(vesselId, "vessel id cannot be null");
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(expectedRevision, "expected vessel revision cannot be null");
    requireNonNull(name, "vessel name cannot be null");
    requireNonNull(nominalCapacity, "vessel nominal capacity cannot be null");
    requireNonNull(material, "vessel material cannot be null");
    requireNonNull(operationalStatus, "vessel operational status cannot be null");
  }
}
