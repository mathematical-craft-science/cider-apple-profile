package org.craft.mcs.cap.core.feedstock.application.measurement.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockMeasurementNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.DeleteFeedstockMeasurementCommand;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.DeleteFeedstockMeasurementUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementRepository;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for deleting one feedstock measurement from a batch history.
 *
 * <p>The service validates the parent batch and ensures the measurement belongs to it before
 * deletion. This prevents cross-batch measurement manipulation through ids from another batch
 * path.
 */
@Component
@RequiredArgsConstructor
public final class DeleteFeedstockMeasurementService
    implements DeleteFeedstockMeasurementUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockMeasurementRepository feedstockMeasurementRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public void delete(@NonNull DeleteFeedstockMeasurementCommand command) {
    requireNonNull(command, "delete feedstock measurement command cannot be null");

    transactionHelper.executeInTransaction(() -> {
      final var batch = feedstockBatchRepository.findById(command.batchId())
          .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
      feedstockAccessControl.requireWriteAccess(batch.workspaceId());
      final var state = feedstockMeasurementRepository.findVersionedById(command.measurementId())
          .orElseThrow(() -> new FeedstockMeasurementNotFoundException(command.measurementId()));
      final var measurement = state.aggregate();

      if (!measurement.batchId().equals(command.batchId())) {
        throw new FeedstockMeasurementNotFoundException(command.measurementId());
      }

      feedstockMeasurementRepository.delete(state);
    });
  }
}
