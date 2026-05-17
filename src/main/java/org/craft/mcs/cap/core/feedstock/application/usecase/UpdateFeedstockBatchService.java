package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.UpdateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for editing feedstock batch metadata and available stock volume.
 *
 * <p>The service is the ownership boundary for workspace edits: it verifies that the requested
 * cider maker owns the batch before applying partial changes to the aggregate. It does not edit
 * chemical profile values because those belong to measurement history.
 */
@Component
@RequiredArgsConstructor
public final class UpdateFeedstockBatchService
    implements UpdateFeedstockBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockBatchSummary update(@NonNull UpdateFeedstockBatchCommand command) {
    requireNonNull(command, "update feedstock batch command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var batch = feedstockBatchRepository.findById(command.batchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));

      if (!batch.ciderMakerId().equals(command.ciderMakerId())) {
        throw new FeedstockBatchOwnershipMismatchException(
            batch.batchId(),
            command.ciderMakerId(),
            batch.ciderMakerId()
        );
      }

      if (command.name() != null) {
        batch.rename(command.name());
      }
      if (command.type() != null) {
        batch.changeType(command.type());
      }
      if (command.season() != null) {
        batch.changeSeason(command.season());
      }
      if (command.clearSource()) {
        batch.clearSource();
      } else if (command.source() != null) {
        batch.changeSource(command.source());
      }
      if (command.availableVolume() != null) {
        batch.changeAvailableVolume(command.availableVolume());
      }
      if (command.clearNote()) {
        batch.clearNote();
      } else if (command.note() != null) {
        batch.changeNote(command.note());
      }

      return toSummary(feedstockBatchRepository.save(batch));
    });
  }

  private static @NonNull FeedstockBatchSummary toSummary(@NonNull FeedstockBatch batch) {
    return new FeedstockBatchSummary(
        batch.batchId(),
        batch.ciderMakerId(),
        batch.name(),
        batch.type(),
        batch.season(),
        batch.source(),
        batch.availableVolume(),
        batch.note()
    );
  }
}
