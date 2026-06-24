package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.DeleteFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.DeleteFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for removing a feedstock batch from active source material.
 *
 * <p>The service checks cider-maker ownership before delegating deletion to the repository. The
 * persistence adapter may implement this as soft delete; the use case expresses the domain intent
 * that the batch should no longer participate in normal feedstock queries.
 */
@Component
@RequiredArgsConstructor
public final class DeleteFeedstockBatchService
    implements DeleteFeedstockBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public void delete(@NonNull DeleteFeedstockBatchCommand command) {
    requireNonNull(command, "delete feedstock batch command cannot be null");

    transactionHelper.executeInTransaction(() -> {
      final var batch = feedstockBatchRepository.findById(command.batchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));

      if (!batch.ciderMakerId().equals(command.ciderMakerId())) {
        throw new FeedstockBatchOwnershipMismatchException(
            batch.batchId(),
            command.ciderMakerId(),
            batch.ciderMakerId()
        );
      }

      feedstockBatchRepository.delete(batch);
    });
  }
}
