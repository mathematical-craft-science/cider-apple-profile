package org.craft.mcs.cap.infrastructure.adapter.out.traceability.feedstock;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.account.user.application.port.in.GetCurrentAccountProfileUseCase;
import org.craft.mcs.cap.core.traceability.application.port.in.AppendBatchTraceStepUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceActorReference;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceMaterialSnapshot;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStep;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchTraceRecorder;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Adapts Feedstock creation to the initial immutable state in Batch Traceability. */
@Component
@RequiredArgsConstructor
final class BatchTraceabilityFeedstockBatchTraceRecorder implements FeedstockBatchTraceRecorder {

  private final @NonNull AppendBatchTraceStepUseCase appendBatchTraceStepUseCase;
  private final @NonNull GetCurrentAccountProfileUseCase getCurrentAccountProfileUseCase;

  @Override
  public void recordCreated(
      @NonNull FeedstockBatch batch,
      @NonNull Instant occurredAt
  ) {
    requireNonNull(batch, "feedstock batch cannot be null");
    requireNonNull(occurredAt, "feedstock trace occurrence time cannot be null");
    final var subject = new BatchTraceSubject(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        batch.batchId().value(),
        batch.name().value()
    );
    final var stateId = batch.currentMaterialStateId();
    final var output = new BatchTraceStepSubject(
        stateId,
        subject,
        BatchTraceMaterialSnapshot.ofVolume(batch.availableVolume())
    );
    final var step = BatchTraceStep.create(
        batch.workspaceId(),
        null,
        BatchTraceStepType.FEEDSTOCK_BATCH_CREATED,
        List.of(),
        List.of(output),
        occurredAt,
        BatchTraceActorReference.accountUser(
            getCurrentAccountProfileUseCase.get().userId().value().toString()
        ),
        batch.note()
    );
    appendBatchTraceStepUseCase.append(step);
  }
}
