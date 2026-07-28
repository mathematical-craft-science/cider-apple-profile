package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchCommand;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.CreateFeedstockBatchUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchCreated;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchTraceRecorder;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.shared.platform.TransactionHelper;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Application service for registering a new feedstock batch in a workspace.
 *
 * <p>The service creates the operational batch aggregate with season/source/available-volume
 * metadata. Chemical data is intentionally not captured here; it is recorded through the independent
 * Measurement context so the batch lifecycle does not own measurement history.
 */
@Component
@RequiredArgsConstructor
public final class CreateFeedstockBatchService
    implements CreateFeedstockBatchUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull FeedstockBatchTraceRecorder traceRecorder;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull TransactionHelper transactionHelper;
  private final @NonNull Clock clock;

  @Override
  public @NonNull FeedstockBatchCreated create(@NonNull CreateFeedstockBatchCommand command) {
    requireNonNull(command, "create feedstock batch command cannot be null");
    feedstockAccessControl.requireWriteAccess(command.workspaceId());

    return transactionHelper.callInTransactionNonNull(() -> {
      final var batch = FeedstockBatch.create(
          FeedstockBatchId.newId(),
          command.workspaceId(),
          command.name(),
          command.vessel(),
          command.type(),
          command.season(),
          command.availableVolume()
      ).changeSource(command.source()).changeNote(command.note());

      final var saved = feedstockBatchRepository.create(batch);
      traceRecorder.recordCreated(saved, clock.instant());

      return new FeedstockBatchCreated(
          saved.batchId(),
          saved.workspaceId(),
          saved.name(),
          saved.vessel(),
          saved.type(),
          saved.season(),
          saved.source(),
          saved.lifecycle(),
          saved.availableVolume(),
          saved.note()
      );
    });
  }
}
