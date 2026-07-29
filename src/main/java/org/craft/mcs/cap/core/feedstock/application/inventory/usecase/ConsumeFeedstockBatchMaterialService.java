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
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialConsumption;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.feedstock.application.inventory.usecase
    .FeedstockMaterialCharacteristicsProjector.project;


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
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
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
    final var sourceStateId = batch.currentMaterialStateId();
    final var measurement = currentProfileQueryService.requireCurrentProfile(batch.batchId());
    final var characteristics = project(measurement);
    final var availableBefore = batch.availableVolume();

    batch.consumeAvailableVolume(command.volume());
    feedstockBatchRepository.update(state.withAggregate(batch));

    return new ConsumedFeedstockBatchMaterial(
        batch.batchId(),
        batch.name(),
        new MaterialConsumption(
            availableBefore,
            command.volume(),
            batch.availableVolume(),
            sourceStateId,
            batch.currentMaterialStateId()
        ),
        FeedstockBatchMaterialEvidenceReference.from(measurement.evidence().stream()
            .map(MeasurementEvidenceView::snapshot)
            .toList()),
        characteristics,
        measurement.aggregateConfidence()
    );
  }

  private @NonNull VersionedAggregate<FeedstockBatch> requireOwnedBatch(
      @NonNull ConsumeFeedstockBatchMaterialCommand command
  ) {
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
