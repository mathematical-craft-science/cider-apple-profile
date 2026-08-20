package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.jspecify.annotations.NonNull;


/** Registers a new Vessel aggregate. */
public interface CreateVesselUseCase {

  @NonNull VesselUsageView create(@NonNull CreateVesselCommand command);
}
