package org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.batch.application.FeedstockBatchVesselChangeNotAllowedException;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.UpdateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchOperationHistory;
import org.craft.mcs.cap.core.feedstock.batch.application.vessel.port.out.FeedstockVesselAssignmentValidator;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for editing feedstock metadata and correcting its initial vessel assignment.
 *
 * <p>The service is the ownership boundary for feedstock batch changes: it verifies that the requested
 * workspace owns the batch before applying partial changes to the aggregate. The initial vessel
 * assignment can be corrected only before the first post-creation material operation.
 */
@Component
@RequiredArgsConstructor
public final class UpdateFeedstockBatchService
    implements UpdateFeedstockBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull FeedstockBatchViewEnricher batchViewEnricher;
  private final @NonNull FeedstockVesselAssignmentValidator vesselAssignmentValidator;
  private final @NonNull FeedstockBatchOperationHistory operationHistory;

  @Override
  public @NonNull FeedstockBatchView update(@NonNull UpdateFeedstockBatchCommand command) {
    requireNonNull(command, "update feedstock batch command cannot be null");
    feedstockAccessControl.requireWriteAccess(command.workspaceId());
    return transactionHelper.callInTransactionNonNull(() -> updateInTransaction(command));
  }

  private @NonNull FeedstockBatchView updateInTransaction(@NonNull UpdateFeedstockBatchCommand command) {
    final var state = findOwnedBatch(command);
    final var batch = state.aggregate();
    applyVesselChange(batch, command);
    applyChanges(batch, command);
    return batchViewEnricher.enrich(
        FeedstockBatchSummaryMapper.toSummary(
            feedstockBatchRepository.update(state.withAggregate(batch)).aggregate()
        )
    );
  }

  private void applyVesselChange(
      @NonNull FeedstockBatch batch,
      @NonNull UpdateFeedstockBatchCommand command
  ) {
    final var vesselId = command.vesselId();
    if (vesselId == null || vesselId.equals(batch.vesselId())) {
      return;
    }
    if (operationHistory.hasOperations(batch.batchId())) {
      throw new FeedstockBatchVesselChangeNotAllowedException(batch.batchId());
    }
    vesselAssignmentValidator.requireAssignable(
        batch.workspaceId(),
        vesselId,
        batch.availableVolume()
    );
    batch.correctInitialVesselAssignment(vesselId);
  }

  private @NonNull VersionedAggregate<FeedstockBatch> findOwnedBatch(
      @NonNull UpdateFeedstockBatchCommand command
  ) {
    final var state = feedstockBatchRepository.findVersionedById(command.batchId())
        .orElseThrow(() -> new FeedstockBatchNotFoundException(command.batchId()));
    final var batch = state.aggregate();
    assertOwnedByWorkspace(batch, command);
    return state;
  }

  private static void assertOwnedByWorkspace(
      @NonNull FeedstockBatch batch,
      @NonNull UpdateFeedstockBatchCommand command
  ) {
    if (!batch.workspaceId().equals(command.workspaceId())) {
      throw new FeedstockBatchOwnershipMismatchException(
          batch.batchId(),
          command.workspaceId(),
          batch.workspaceId()
      );
    }
  }

  private static void applyChanges(
      @NonNull FeedstockBatch batch,
      @NonNull UpdateFeedstockBatchCommand command
  ) {
    final var name = command.name();
    if (name != null) {
      batch.rename(name);
    }
    final var type = command.type();
    if (type != null) {
      batch.changeType(type);
    }
    final var season = command.season();
    if (season != null) {
      batch.changeSeason(season);
    }
    applySourceChange(batch, command);
    applyNoteChange(batch, command);
  }

  private static void applySourceChange(
      @NonNull FeedstockBatch batch,
      @NonNull UpdateFeedstockBatchCommand command
  ) {
    if (command.clearSource()) {
      batch.clearSource();
      return;
    }
    final var source = command.source();
    if (source != null) {
      batch.changeSource(source);
    }
  }

  private static void applyNoteChange(
      @NonNull FeedstockBatch batch,
      @NonNull UpdateFeedstockBatchCommand command
  ) {
    if (command.clearNote()) {
      batch.clearNote();
      return;
    }
    final var note = command.note();
    if (note != null) {
      batch.changeNote(note);
    }
  }

}
