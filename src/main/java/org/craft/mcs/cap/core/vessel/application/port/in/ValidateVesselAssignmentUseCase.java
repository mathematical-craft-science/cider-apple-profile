package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.shared.application.material.VesselReferenceView;
import org.jspecify.annotations.NonNull;


/**
 * Locks and validates that a workspace vessel can receive a material volume.
 *
 * <p>The caller must keep the surrounding transaction open until the new material batch has been
 * persisted. Releasing the vessel lock before persistence would allow two concurrent assignments.</p>
 */
public interface ValidateVesselAssignmentUseCase {

  @NonNull VesselReferenceView validate(@NonNull ValidateVesselAssignmentCommand command);
}
