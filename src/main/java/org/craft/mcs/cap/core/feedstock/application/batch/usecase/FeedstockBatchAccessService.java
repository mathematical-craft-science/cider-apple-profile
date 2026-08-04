package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchAccessUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
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
    final var workspaceId = workspaceId(batchId);
    accessControl.requireReadAccess(workspaceId);
    return workspaceId;
  }

  @Override
  public @NonNull WorkspaceId requireWriteAccess(@NonNull FeedstockBatchId batchId) {
    final var workspaceId = workspaceId(batchId);
    accessControl.requireWriteAccess(workspaceId);
    return workspaceId;
  }

  @Override
  public void requireWorkspaceReadAccess(@NonNull WorkspaceId workspaceId) {
    accessControl.requireReadAccess(requireNonNull(workspaceId, "feedstock workspace id cannot be null"));
  }

  private @NonNull WorkspaceId workspaceId(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return repository.findById(batchId)
        .orElseThrow(() -> new FeedstockBatchNotFoundException(batchId))
        .workspaceId();
  }
}
