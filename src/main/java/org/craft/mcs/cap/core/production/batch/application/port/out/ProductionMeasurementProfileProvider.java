package org.craft.mcs.cap.core.production.batch.application.port.out;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionMeasuredProfileView;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Outbound boundary for current measurement evidence associated with Production batches. */
public interface ProductionMeasurementProfileProvider {

  @NonNull Optional<@NonNull ProductionMeasuredProfileView> findCurrent(
      @NonNull ProductionBatchId batchId
  );

  @NonNull Map<@NonNull ProductionBatchId, @NonNull ProductionMeasuredProfileView> findAllCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  );
}
