package org.craft.mcs.cap.core.feedstock.application.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.CreateFeedstockMeasurementUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementCreated;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurementId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class CreateFeedstockMeasurementService
    implements CreateFeedstockMeasurementUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockMeasurementRepository feedstockMeasurementRepository;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockMeasurementCreated create(@NonNull CreateFeedstockMeasurementCommand command) {
    requireNonNull(command, "create feedstock measurement command cannot be null");

    return transactionHelper.callInTransactionNonNull(() -> {
      final var batch = feedstockBatchRepository.findById(command.batchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));

      final var sample = FeedstockMeasurement.create(
          FeedstockMeasurementId.newId(),
          command.batchId(),
          command.name(),
          command.chemicalProfile(),
          command.measuredAt()
      ).changeNote(command.note());

      final var saved = feedstockMeasurementRepository.save(sample);

      return new FeedstockMeasurementCreated(
          saved.measurementId(),
          batch.ciderMakerId(),
          saved.batchId(),
          saved.name()
      );
    });
  }
}
