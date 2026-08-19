package org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.DeleteFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.DeleteFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for removing an inactive feedstock batch from normal workspace queries.
 *
 * <p>The service checks workspace ownership and asks the aggregate to permit soft deletion. Active
 * material cannot be hidden: a material operation must first deplete or discard it.
 */
@Component
@RequiredArgsConstructor
public final class DeleteFeedstockBatchService
    implements DeleteFeedstockBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public void delete(@NonNull DeleteFeedstockBatchCommand command) {
    requireNonNull(command, "delete feedstock batch command cannot be null");
    feedstockAccessControl.requireWriteAccess(command.workspaceId());

    transactionHelper.executeInTransaction(() -> {
      final var state = feedstockBatchRepository.findVersionedById(command.batchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
      final var batch = state.aggregate();

      if (!batch.workspaceId().equals(command.workspaceId())) {
        throw new FeedstockBatchOwnershipMismatchException(
            batch.batchId(),
            command.workspaceId(),
            batch.workspaceId()
        );
      }

      batch.delete(clock.instant());
      feedstockBatchRepository.update(state.withAggregate(batch));
    });
  }
}
