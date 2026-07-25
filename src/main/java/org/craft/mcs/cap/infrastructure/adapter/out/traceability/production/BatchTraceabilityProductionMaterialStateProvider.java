package org.craft.mcs.cap.infrastructure.adapter.out.traceability.production;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.batchtraceability.application.port.in.GetCurrentMaterialStateUseCase;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubject;
import org.craft.mcs.cap.core.batchtraceability.domain.model.BatchTraceSubjectType;
import org.craft.mcs.cap.core.production.application.port.out.ProductionMaterialStateProvider;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Resolves Production current-state identity through the Batch Traceability boundary. */
@Component
@RequiredArgsConstructor
final class BatchTraceabilityProductionMaterialStateProvider implements ProductionMaterialStateProvider {

  private final @NonNull GetCurrentMaterialStateUseCase getCurrentMaterialStateUseCase;

  @Override
  public @NonNull MaterialStateId currentStateOf(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");
    return getCurrentMaterialStateUseCase.getCurrent(toSubject(batchId)).materialStateId();
  }

  @Override
  public @NonNull Map<@NonNull ProductionBatchId, @NonNull MaterialStateId> currentStatesOf(
      @NonNull WorkspaceId workspaceId,
      @NonNull Set<@NonNull ProductionBatchId> batchIds
  ) {
    requireNonNull(workspaceId, "production workspace id cannot be null");
    requireNonNull(batchIds, "production batch ids cannot be null");
    final var subjects = batchIds.stream()
        .map(BatchTraceabilityProductionMaterialStateProvider::toSubject)
        .collect(Collectors.toUnmodifiableSet());
    return getCurrentMaterialStateUseCase.getCurrent(workspaceId, subjects)
        .entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(
            entry -> ProductionBatchId.of(entry.getKey().id()),
            entry -> entry.getValue().materialStateId()
        ));
  }

  private static @NonNull BatchTraceSubject toSubject(@NonNull ProductionBatchId batchId) {
    return BatchTraceSubject.of(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        batchId.value()
    );
  }
}
