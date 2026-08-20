package org.craft.mcs.cap.core.vessel.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.vessel.application.contract.VesselUsageView;
import org.jspecify.annotations.NonNull;


/** Lists vessels registered in a workspace catalog. */
public interface ListVesselsUseCase {

  @NonNull List<@NonNull VesselUsageView> list(@NonNull ListVesselsQuery query);
}
