package org.craft.mcs.cap.infrastructure.adapter.out.traceability.feedstock;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.GetCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.feedstock.application.inventory.port.out.FeedstockMaterialStateProvider;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves Feedstock current-state identity through the Batch Traceability boundary. */
@Component
@RequiredArgsConstructor
final class BatchTraceabilityFeedstockMaterialStateProvider implements FeedstockMaterialStateProvider {

  private final @NonNull GetCurrentMaterialStateUseCase getCurrentMaterialStateUseCase;

  @Override
  public @NonNull MaterialStateId currentStateOf(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return getCurrentMaterialStateUseCase.getCurrent(toSubject(batchId)).materialStateId();
  }

  @Override
  public @NonNull Map<@NonNull FeedstockBatchId, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull FeedstockBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "feedstock workspace id cannot be null");
    requireNonNull(batchIds, "feedstock batch ids cannot be null");
    final var subjects = batchIds.stream()
        .map(BatchTraceabilityFeedstockMaterialStateProvider::toSubject)
        .collect(Collectors.toUnmodifiableSet());
    return getCurrentMaterialStateUseCase.getCurrent(workspaceId, subjects)
        .entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> FeedstockBatchId.of(entry.getKey().id()),
            entry -> entry.getValue().materialStateId()
        ));
  }

  private static @NonNull BatchTraceSubject toSubject(@NonNull FeedstockBatchId batchId) {
    return BatchTraceSubject.of(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        batchId.value()
    );
  }
}
