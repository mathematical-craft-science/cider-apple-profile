package org.craft.mcs.cap.core.feedstock.application.inventory.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialName;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialSnapshot;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.InspectFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.InspectFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.out.FeedstockMaterialStateProvider;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads current feedstock stock and measurement evidence without changing available volume. */
@Component
@RequiredArgsConstructor
public final class InspectFeedstockBatchMaterialService implements InspectFeedstockBatchMaterialUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull FeedstockMaterialStateProvider materialStateProvider;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockBatchMaterialSnapshot inspect(
      @NonNull InspectFeedstockBatchMaterialCommand command
  ) {
    requireNonNull(command, "inspect feedstock batch material command cannot be null");
    feedstockAccessControl.requireReadAccess(command.workspaceId());
    return transactionHelper.callInTransactionNonNull(() -> inspectInTransaction(command));
  }

  private @NonNull FeedstockBatchMaterialSnapshot inspectInTransaction(
      @NonNull InspectFeedstockBatchMaterialCommand command
  ) {
    final var batch = feedstockBatchRepository.findById(command.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
    if (!batch.workspaceId().equals(command.workspaceId())) {
      throw new FeedstockBatchOwnershipMismatchException(
          batch.batchId(),
          command.workspaceId(),
          batch.workspaceId()
      );
    }
    if (command.requestedVolume().exceeds(batch.availableVolume())) {
      throw new FeedstockBatchInsufficientAvailableVolumeException(
          batch.batchId(),
          command.requestedVolume(),
          batch.availableVolume()
      );
    }
    final var measurement = currentMeasurementQueryService.requireCurrentMeasurement(batch.batchId());
    return new FeedstockBatchMaterialSnapshot(
        batch.batchId(),
        FeedstockBatchMaterialName.of(batch.name().value()),
        command.requestedVolume(),
        batch.availableVolume(),
        materialStateProvider.currentStateOf(batch.batchId()),
        new FeedstockBatchMaterialEvidenceReference(
            measurement.measurementId().value(),
            measurement.evidenceVersion()
        ),
        measurement.chemicalProfile(),
        measurement.aggregateConfidence()
    );
  }
}
