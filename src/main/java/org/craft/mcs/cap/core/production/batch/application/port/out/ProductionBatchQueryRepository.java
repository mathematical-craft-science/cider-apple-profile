package org.craft.mcs.cap.core.production.batch.application.port.out;

import java.util.List;
import java.util.Optional;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Read-model repository for production batch query scenarios. */
public interface ProductionBatchQueryRepository {

  @NonNull Optional<@NonNull ProductionBatchView> findById(@NonNull ProductionBatchId batchId);

  @NonNull List<@NonNull ProductionBatchView> findAllByWorkspaceId(@NonNull WorkspaceId workspaceId);
}
