package org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchMaterialSnapshotChangedException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumeFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumeFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ConsumedFeedstockBatchMaterial;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialConsumption;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase
    .FeedstockMaterialAvailabilityPolicy.requireAvailableVolume;
import static org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase
    .FeedstockMaterialAvailabilityPolicy.requireOwnedBy;
import static org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase
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
    final var state = feedstockBatchRepository.lockVersionedById(command.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
    final var batch = state.aggregate();
    requireOwnedBy(batch, command.workspaceId());
    final var sourceStateId = batch.currentMaterialStateId();
    if (!sourceStateId.equals(command.expectation().materialStateId())) {
      throw new FeedstockBatchMaterialSnapshotChangedException(batch.batchId());
    }
    final var measurement = requireCurrentProfile(batch.batchId());
    final var evidenceReference = FeedstockBatchMaterialEvidenceReference.from(
        measurement.evidence().stream()
            .map(MeasurementEvidenceView::snapshot)
            .toList()
    );
    if (!evidenceReference.equals(command.expectation().evidenceReference())) {
      throw new FeedstockBatchMaterialSnapshotChangedException(batch.batchId());
    }
    requireAvailableVolume(batch, command.volume());
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
        evidenceReference,
        characteristics,
        measurement.aggregateConfidence()
    );
  }

  private @NonNull FeedstockCurrentProfileView requireCurrentProfile(@NonNull FeedstockBatchId batchId) {
    try {
      return currentProfileQueryService.requireCurrentProfile(batchId);
    } catch (FeedstockBatchHasNoMeasurementsException _) {
      throw new FeedstockBatchMaterialSnapshotChangedException(batchId);
    }
  }

}
