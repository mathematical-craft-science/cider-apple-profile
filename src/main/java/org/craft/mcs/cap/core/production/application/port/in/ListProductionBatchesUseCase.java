package org.craft.mcs.cap.core.production.application.port.in;

import java.util.List;
import org.jspecify.annotations.NonNull;


public interface ListProductionBatchesUseCase {

  @NonNull
  List<@NonNull ProductionBatchView> list(@NonNull ListProductionBatchesQuery query);
}
