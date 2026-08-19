package org.craft.mcs.cap.core.production.batch.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.port.in.ProductionBatchAccessUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Authorizes cross-context access to a Production batch without exposing its aggregate. */
@Component
@RequiredArgsConstructor
public final class ProductionBatchAccessService implements ProductionBatchAccessUseCase {

  private final @NonNull ProductionBatchRepository repository;
  private final @NonNull ProductionAccessControl accessControl;

  @Override
  public @NonNull WorkspaceId requireReadAccess(@NonNull ProductionBatchId batchId) {
    final var workspaceId = workspaceId(batchId);
    accessControl.requireReadAccess(workspaceId);
    return workspaceId;
  }

  @Override
  public @NonNull WorkspaceId requireWriteAccess(@NonNull ProductionBatchId batchId) {
    final var batch = batch(batchId);
    accessControl.requireWriteAccess(batch.workspaceId());
    return batch.workspaceId();
  }

  @Override
  public void requireWorkspaceReadAccess(@NonNull WorkspaceId workspaceId) {
    accessControl.requireReadAccess(requireNonNull(workspaceId, "production workspace id cannot be null"));
  }

  private @NonNull WorkspaceId workspaceId(@NonNull ProductionBatchId batchId) {
    return batch(batchId).workspaceId();
  }

  private @NonNull ProductionBatch batch(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");
    return repository.findById(batchId)
        .orElseThrow(() -> new ProductionBatchNotFoundException(batchId));
  }

}
