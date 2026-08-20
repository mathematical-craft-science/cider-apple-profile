package org.craft.mcs.cap.core.vessel.application.port.in;

import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.jspecify.annotations.NonNull;


/** Reads one vessel from the workspace catalog. */
public interface GetVesselUseCase {

  @NonNull VesselUsageView get(@NonNull GetVesselQuery query);
}
