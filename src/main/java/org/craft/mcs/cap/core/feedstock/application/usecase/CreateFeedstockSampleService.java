package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockSampleUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleCreated;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSample;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.FeedstockSampleId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class CreateFeedstockSampleService
    implements CreateFeedstockSampleUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockSampleRepository feedstockSampleRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockSampleCreated create(@NonNull CreateFeedstockSampleCommand command) {
    requireNonNull(command, "create feedstock sample command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var batch = feedstockBatchRepository.findById(command.batchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));

      final var sample = FeedstockSample.create(
          FeedstockSampleId.newId(),
          command.batchId(),
          command.name(),
          command.type(),
          command.chemicalProfile(),
          command.measuredAt()
      ).changeNote(command.note());

      final var saved = feedstockSampleRepository.save(sample);

      return new FeedstockSampleCreated(
          saved.sampleId(),
          batch.ciderMakerId(),
          saved.batchId(),
          saved.name(),
          saved.type()
      );
    });
  }
}
