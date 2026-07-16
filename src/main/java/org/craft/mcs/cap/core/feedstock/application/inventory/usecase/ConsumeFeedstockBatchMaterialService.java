package org.craft.mcs.cap.core.feedstock.application.inventory.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumeFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumeFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.ConsumedFeedstockBatchMaterial;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialName;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Consumes feedstock stock and captures the current measurement in one transaction.
 *
 * <p>This use case is the inbound Feedstock boundary for external material consumers. It keeps
 * aggregate ownership, stock invariants, current-measurement semantics and persistence inside the
 * Feedstock context.
 */
@Component
@RequiredArgsConstructor
public final class ConsumeFeedstockBatchMaterialService
    implements ConsumeFeedstockBatchMaterialUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull ConsumedFeedstockBatchMaterial consume(
      @NonNull ConsumeFeedstockBatchMaterialCommand command
  ) {
    requireNonNull(command, "consume feedstock batch material command cannot be null");
    feedstockAccessControl.requireWriteAccess(command.workspaceId());
    return transactionHelper.callInTransactionNonNull(() -> consumeInTransaction(command));
  }

  private @NonNull ConsumedFeedstockBatchMaterial consumeInTransaction(
      @NonNull ConsumeFeedstockBatchMaterialCommand command
  ) {
    final var state = requireOwnedBatch(command);
    final var batch = state.aggregate();
    requireAvailableVolume(batch, command);
    final var measurement = currentMeasurementQueryService.requireCurrentMeasurement(batch.batchId());

    batch.consumeAvailableVolume(command.volume());
    feedstockBatchRepository.update(state.withAggregate(batch));

    return new ConsumedFeedstockBatchMaterial(
        batch.batchId(),
        FeedstockBatchMaterialName.of(batch.name().value()),
        command.volume(),
        new FeedstockBatchMaterialEvidenceReference(
            measurement.measurementId().value(),
            measurement.evidenceVersion()
        ),
        measurement.chemicalProfile()
    );
  }

  private @NonNull VersionedAggregate<FeedstockBatch> requireOwnedBatch(
      @NonNull ConsumeFeedstockBatchMaterialCommand command
  ) {
    final var state = feedstockBatchRepository.findByIdForConsumption(command.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
    final var batch = state.aggregate();

    if (!batch.workspaceId().equals(command.workspaceId())) {
      throw new FeedstockBatchOwnershipMismatchException(
          batch.batchId(),
          command.workspaceId(),
          batch.workspaceId()
      );
    }
    return state;
  }

  private static void requireAvailableVolume(
      @NonNull FeedstockBatch batch,
      @NonNull ConsumeFeedstockBatchMaterialCommand command
  ) {
    if (command.volume().exceeds(batch.availableVolume())) {
      throw new FeedstockBatchInsufficientAvailableVolumeException(
          batch.batchId(),
          command.volume(),
          batch.availableVolume()
      );
    }
  }
}
