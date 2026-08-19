package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.production.batch.entity.ProductionBatchJdbcMapper;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC read adapter for production batch list and details projections. */
@Repository
@RequiredArgsConstructor
class JdbcProductionBatchQueryRepository implements ProductionBatchQueryRepository {

  private final @NonNull SpringDataProductionBatchJdbcRepository repository;

  @Override
  public @NonNull Optional<@NonNull ProductionBatchView> findById(
      @NonNull ProductionBatchId batchId
  ) {
    requireNonNull(batchId, "production batch id cannot be null");
    return repository.findById(batchId.value())
        .map(ProductionBatchJdbcMapper::toView);
  }

  @Override
  public @NonNull List<@NonNull ProductionBatchView> findAllByWorkspaceId(
      @NonNull WorkspaceId workspaceId
  ) {
    requireNonNull(workspaceId, "workspace id cannot be null");
    return repository.findAllByWorkspaceIdOrderByCreatedAtDescBatchIdDesc(workspaceId.value()).stream()
        .map(ProductionBatchJdbcMapper::toView)
        .toList();
  }
}
