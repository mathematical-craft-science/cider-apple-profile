package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.QueryFeedstockMaterialStatesUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockMaterialStateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves authoritative Feedstock material states within an authorized workspace. */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockMaterialStatesService implements QueryFeedstockMaterialStatesUseCase {

  private final @NonNull FeedstockAccessControl accessControl;
  private final @NonNull FeedstockMaterialStateQuery materialStateQuery;

  @Override
  public @NonNull Map<@NonNull FeedstockBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "feedstock workspace id cannot be null");
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    accessControl.requireReadAccess(workspaceId);
    if (batchIds.isEmpty()) {
      return Map.of();
    }

    final var states = materialStateQuery.findCurrentStates(workspaceId, Set.copyOf(batchIds));
    batchIds.stream()
        .filter(batchId -> !states.containsKey(batchId))
        .findFirst()
        .ifPresent(batchId -> {
          throw new FeedstockBatchNotFoundException(batchId);
        });
    return states;
  }
}
