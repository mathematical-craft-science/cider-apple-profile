package org.craft.mcs.cap.core.production.batch.application.port.in;

import java.util.List;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchDetailsView;
import org.jspecify.annotations.NonNull;


public interface ListProductionBatchesUseCase {

  @NonNull
  List<@NonNull ProductionBatchDetailsView> list(@NonNull ListProductionBatchesQuery query);
}
