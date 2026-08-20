package org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchHasNoMeasurementsException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchMaterialSnapshotChangedException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query.FeedstockCurrentProfileQueryService;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.FeedstockBatchMaterialEvidenceReference;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ValidateFeedstockBatchMaterialSnapshotCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.inventory.port.in.ValidateFeedstockBatchMaterialSnapshotUseCase;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.application.measurement.MeasurementEvidenceView;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase
    .FeedstockMaterialAvailabilityPolicy.requireAvailableVolume;
import static org.craft.mcs.cap.core.feedstock.batch.application.inventory.usecase
    .FeedstockMaterialAvailabilityPolicy.requireOwnedBy;


/** Performs the short Feedstock-owned concurrency check before a planning snapshot is persisted. */
@Component
@RequiredArgsConstructor
public final class ValidateFeedstockBatchMaterialSnapshotService
    implements ValidateFeedstockBatchMaterialSnapshotUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockCurrentProfileQueryService currentProfileQueryService;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public void validate(@NonNull ValidateFeedstockBatchMaterialSnapshotCommand command) {
    requireNonNull(command, "validate feedstock material snapshot command cannot be null");
    feedstockAccessControl.requireReadAccess(command.workspaceId());
    transactionHelper.executeInTransaction(() -> validateInTransaction(command));
  }

  private void validateInTransaction(@NonNull ValidateFeedstockBatchMaterialSnapshotCommand command) {
    final var state = feedstockBatchRepository.lockVersionedById(command.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
    final var batch = state.aggregate();
    requireOwnedBy(batch, command.workspaceId());
    if (!state.revision().equals(command.aggregateRevision())
        || !batch.currentMaterialStateId().equals(command.expectation().materialStateId())) {
      throw new FeedstockBatchMaterialSnapshotChangedException(batch.batchId());
    }
    final var evidenceReference = FeedstockBatchMaterialEvidenceReference.from(
        requireCurrentProfile(batch.batchId()).evidence().stream()
            .map(MeasurementEvidenceView::snapshot)
            .toList()
    );
    if (!evidenceReference.equals(command.expectation().evidenceReference())) {
      throw new FeedstockBatchMaterialSnapshotChangedException(batch.batchId());
    }
    requireAvailableVolume(batch, command.requestedVolume());
  }

  private @NonNull FeedstockCurrentProfileView requireCurrentProfile(@NonNull FeedstockBatchId batchId) {
    try {
      return currentProfileQueryService.requireCurrentProfile(batchId);
    } catch (FeedstockBatchHasNoMeasurementsException _) {
      throw new FeedstockBatchMaterialSnapshotChangedException(batchId);
    }
  }
}
