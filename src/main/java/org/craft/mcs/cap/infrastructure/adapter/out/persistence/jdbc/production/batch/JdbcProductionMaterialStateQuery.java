package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionMaterialStateQuery;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch.entity.ProductionBatchJdbcMapper;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC read adapter for material-state identities owned by Production batches. */
@Repository
@RequiredArgsConstructor
class JdbcProductionMaterialStateQuery implements ProductionMaterialStateQuery {

  private final @NonNull SpringDataProductionBatchJdbcRepository repository;

  @Override
  public @NonNull Map<@NonNull ProductionBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "production workspace id cannot be null");
    requireNonNull(batchIds, "production batch ids cannot be null");
    if (batchIds.isEmpty()) {
      return Map.of();
    }

    return repository.findAllByBatchIdInAndWorkspaceId(
            batchIds.stream().map(ProductionBatchId::value).toList(),
            workspaceId.value()
        )
        .stream()
        .map(ProductionBatchJdbcMapper::toView)
        .collect(Collectors.toUnmodifiableMap(
            ProductionBatchView::batchId,
            ProductionBatchView::currentMaterialStateId
        ));
  }
}
