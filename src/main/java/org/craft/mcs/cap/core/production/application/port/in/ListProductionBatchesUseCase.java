package org.craft.mcs.cap.core.production.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.jspecify.annotations.NonNull;


public interface ListProductionBatchesUseCase {

  @NonNull
  List<@NonNull ProductionBatch> list(@NonNull ListProductionBatchesQuery query);
}
