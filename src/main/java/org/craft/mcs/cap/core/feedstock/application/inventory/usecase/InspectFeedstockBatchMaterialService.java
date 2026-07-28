package org.craft.mcs.cap.core.feedstock.application.inventory.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchInsufficientAvailableVolumeException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.FeedstockBatchMaterialSnapshot;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.InspectFeedstockBatchMaterialCommand;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.in.InspectFeedstockBatchMaterialUseCase;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.kernel.material.value.MaterialAvailability;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Reads current feedstock stock and measurement evidence without changing available volume. */
@Component
@RequiredArgsConstructor
public final class InspectFeedstockBatchMaterialService implements InspectFeedstockBatchMaterialUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
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
    final var measurement = currentProfileQueryService.requireCurrentProfile(batch.batchId());
    return new FeedstockBatchMaterialSnapshot(
        batch.batchId(),
        batch.name(),
        new MaterialAvailability(
            command.requestedVolume(),
            batch.availableVolume(),
            batch.currentMaterialStateId()
        ),
        FeedstockBatchMaterialEvidenceReference.from(measurement.evidence().stream()
            .map(MeasurementEvidenceView::snapshot)
            .toList()),
        measurement.chemicalProfile(),
        measurement.aggregateConfidence()
    );
  }
}
