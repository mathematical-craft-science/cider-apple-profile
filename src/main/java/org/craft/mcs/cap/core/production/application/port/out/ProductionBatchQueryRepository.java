package org.craft.mcs.cap.core.production.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchView;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.jspecify.annotations.NonNull;


/**
 * Read-side repository for production batch query views.
 */
public interface ProductionBatchQueryRepository {

  @NonNull
  Optional<@NonNull ProductionBatchView> findViewById(@NonNull ProductionBatchId batchId);

  @NonNull
  List<@NonNull ProductionBatchView> findViewsByCiderMakerId(@NonNull CiderMakerId ciderMakerId);
}
