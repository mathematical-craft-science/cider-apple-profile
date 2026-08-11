package org.craft.mcs.cap.core.vessel.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.vessel.application.model.VesselUsageView;
import org.jspecify.annotations.NonNull;


/** Lists operational empty vessels that may be selected for a subsequent material assignment. */
public interface ListAvailableVesselsUseCase {

  @NonNull List<@NonNull VesselUsageView> listAvailable(@NonNull ListAvailableVesselsQuery query);
}
