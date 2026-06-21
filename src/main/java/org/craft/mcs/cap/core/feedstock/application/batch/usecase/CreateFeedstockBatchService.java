package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCreated;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for registering a new feedstock batch in a cider maker workspace.
 *
 * <p>The service creates the operational batch aggregate with season/source/available-volume
 * metadata. Chemical data is intentionally not captured here; it is added through
 * {@code CreateFeedstockMeasurementService} so the batch can keep a measurement history.
 */
@Component
@RequiredArgsConstructor
public final class CreateFeedstockBatchService
    implements CreateFeedstockBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockBatchCreated create(@NonNull CreateFeedstockBatchCommand command) {
    requireNonNull(command, "create feedstock batch command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var batch = FeedstockBatch.create(
          FeedstockBatchId.newId(),
          command.ciderMakerId(),
          command.name(),
          command.type(),
          command.season(),
          command.availableVolume()
      ).changeSource(command.source()).changeNote(command.note());

      final var saved = feedstockBatchRepository.save(batch);

      return new FeedstockBatchCreated(
          saved.batchId(),
          saved.ciderMakerId(),
          saved.name(),
          saved.type(),
          saved.season(),
          saved.source(),
          saved.availableVolume(),
          saved.note()
      );
    });
  }
}
