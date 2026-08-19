package org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.FeedstockBatchAccessUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Authorizes cross-context access to a Feedstock batch without exposing its aggregate. */
@Component
@RequiredArgsConstructor
public final class FeedstockBatchAccessService implements FeedstockBatchAccessUseCase {

  private final @NonNull FeedstockBatchRepository repository;
  private final @NonNull FeedstockAccessControl accessControl;

  @Override
  public @NonNull WorkspaceId requireReadAccess(@NonNull FeedstockBatchId batchId) {
    final var batch = batch(batchId);
    accessControl.requireReadAccess(batch.workspaceId());
    return batch.workspaceId();
  }

  @Override
  public @NonNull WorkspaceId requireWriteAccess(@NonNull FeedstockBatchId batchId) {
    final var batch = batch(batchId);
    accessControl.requireWriteAccess(batch.workspaceId());
    return batch.workspaceId();
  }

  @Override
  public void requireWorkspaceReadAccess(@NonNull WorkspaceId workspaceId) {
    accessControl.requireReadAccess(requireNonNull(workspaceId, "feedstock workspace id cannot be null"));
  }

  private @NonNull FeedstockBatch batch(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return repository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId));
  }

}
