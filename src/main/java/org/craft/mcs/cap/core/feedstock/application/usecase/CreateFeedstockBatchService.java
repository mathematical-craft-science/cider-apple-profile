package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCreated;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


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
          command.season(),
          command.availableVolume()
      ).changeSource(command.source()).changeNote(command.note());

      final var saved = feedstockBatchRepository.save(batch);

      return new FeedstockBatchCreated(
          saved.batchId(),
          saved.ciderMakerId(),
          saved.name(),
          saved.season(),
          saved.source(),
          saved.availableVolume(),
          saved.note()
      );
    });
  }
}
