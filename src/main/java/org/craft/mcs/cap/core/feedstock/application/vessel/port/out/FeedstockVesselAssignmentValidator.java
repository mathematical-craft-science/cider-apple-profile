package org.craft.mcs.cap.core.feedstock.application.vessel.port.out;

import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;


/**
 * Feedstock-owned ACL port for locking and validating a new material assignment to a vessel.
 *
 * <p>The lock remains effective only while the caller's batch-creation transaction is open.</p>
 */
public interface FeedstockVesselAssignmentValidator {

  @NonNull VesselReferenceView requireAssignable(
      @NonNull WorkspaceId workspaceId,
      @NonNull VesselId vesselId,
      @NonNull VolumeValue materialVolume
  );
}
