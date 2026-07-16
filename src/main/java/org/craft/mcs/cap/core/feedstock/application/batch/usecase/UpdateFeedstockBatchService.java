package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchNotFoundException;
import org.craft.mcs.cap.core.feedstock.application.FeedstockBatchOwnershipMismatchException;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.UpdateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.UpdateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.craft.mcs.cap.shared.application.persistence.VersionedAggregate;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for editing feedstock batch metadata and available stock volume.
 *
 * <p>The service is the ownership boundary for feedstock batch changes: it verifies that the requested
 * workspace owns the batch before applying partial changes to the aggregate. It does not edit
 * chemical profile values because those belong to measurement history.
 */
@Component
@RequiredArgsConstructor
public final class UpdateFeedstockBatchService
    implements UpdateFeedstockBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;

  @Override
  public @NonNull FeedstockBatchSummary update(@NonNull UpdateFeedstockBatchCommand command) {
    requireNonNull(command, "update feedstock batch command cannot be null");
    feedstockAccessControl.requireWriteAccess(command.workspaceId());
    return transactionHelper.callInTransactionNonNull(() -> updateInTransaction(command));
  }

  private @NonNull FeedstockBatchSummary updateInTransaction(@NonNull UpdateFeedstockBatchCommand command) {
    final var state = findOwnedBatch(command);
    final var batch = state.aggregate();
    applyChanges(batch, command);
    batch.ensureAvailableVolumeIsPositive();
    return toSummary(feedstockBatchRepository.update(state.withAggregate(batch)).aggregate());
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
    applyVesselChange(batch, command);
    final var type = command.type();
    if (type != null) {
      batch.changeType(type);
    }
    final var season = command.season();
    if (season != null) {
      batch.changeSeason(season);
    }
    applySourceChange(batch, command);
    final var availableVolume = command.availableVolume();
    if (availableVolume != null) {
      batch.changeAvailableVolume(availableVolume);
    }
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

  private static void applyVesselChange(
      @NonNull FeedstockBatch batch,
      @NonNull UpdateFeedstockBatchCommand command
  ) {
    if (command.clearVessel()) {
      batch.clearVessel();
      return;
    }
    final var vessel = command.vessel();
    if (vessel != null) {
      batch.changeVessel(vessel);
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

  private static @NonNull FeedstockBatchSummary toSummary(@NonNull FeedstockBatch batch) {
    return new FeedstockBatchSummary(
        batch.batchId(),
        batch.workspaceId(),
        batch.name(),
        batch.vessel(),
        batch.type(),
        batch.season(),
        batch.source(),
        batch.availableVolume(),
        batch.note()
    );
  }
}
