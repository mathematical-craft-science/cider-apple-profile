package org.craft.mcs.cap.core.production.application.port.out;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProfileView;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;


/** Outbound boundary for current measurement evidence associated with Production batches. */
public interface ProductionMeasurementProfileProvider {

  @NonNull Optional<@NonNull ProductionMeasurementProfileView> findCurrent(
      @NonNull ProductionBatchId batchId
  );

  @NonNull Map<@NonNull ProductionBatchId, @NonNull ProductionMeasurementProfileView> findAllCurrent(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  );
}
