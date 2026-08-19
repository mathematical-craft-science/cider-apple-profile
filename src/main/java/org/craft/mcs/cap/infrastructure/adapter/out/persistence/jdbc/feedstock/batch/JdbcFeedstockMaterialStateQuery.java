package org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockMaterialStateQuery;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.infrastructure.adapter.out.persistence.jdbc.feedstock.batch.entity.FeedstockBatchJdbcMapper;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import static java.util.Objects.requireNonNull;


/** JDBC read adapter for material-state identities owned by Feedstock batches. */
@Repository
@RequiredArgsConstructor
class JdbcFeedstockMaterialStateQuery implements FeedstockMaterialStateQuery {

  private final @NonNull SpringDataFeedstockBatchJdbcRepository repository;

  @Override
  public @NonNull Map<@NonNull FeedstockBatchId, @NonNull MaterialStateId> findCurrentStates(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "feedstock workspace id cannot be null");
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    if (batchIds.isEmpty()) {
      return Map.of();
    }

    return repository.findAllByBatchIdInAndWorkspaceIdAndDeletedAtIsNull(
            batchIds.stream().map(FeedstockBatchId::value).toList(),
            workspaceId.value()
        )
        .stream()
        .map(FeedstockBatchJdbcMapper::toSummary)
        .collect(Collectors.toUnmodifiableMap(
            FeedstockBatchSummary::batchId,
            FeedstockBatchSummary::currentMaterialStateId
        ));
  }
}
