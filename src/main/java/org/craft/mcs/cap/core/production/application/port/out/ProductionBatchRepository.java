package org.craft.mcs.cap.core.production.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;


public interface ProductionBatchRepository {

  @NonNull
  ProductionBatch save(@NonNull ProductionBatch productionBatch);

  @NonNull
  Optional<@NonNull ProductionBatch> findById(@NonNull ProductionBatchId batchId);

  @NonNull
  List<@NonNull ProductionBatch> findAllByCiderMakerId(@NonNull CiderMakerId ciderMakerId);
}
