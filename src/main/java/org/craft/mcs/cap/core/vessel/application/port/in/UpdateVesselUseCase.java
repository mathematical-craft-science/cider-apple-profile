package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.core.vessel.application.model.VesselUsageView;
import org.jspecify.annotations.NonNull;


/** Updates catalog characteristics and operational status of a vessel. */
public interface UpdateVesselUseCase {

  @NonNull VesselUsageView update(@NonNull UpdateVesselCommand command);
}
