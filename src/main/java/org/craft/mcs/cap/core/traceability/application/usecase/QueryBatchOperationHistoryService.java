package org.craft.mcs.cap.core.traceability.application.usecase;

import org.craft.mcs.cap.core.traceability.application.port.in.BatchOperationHistoryQuery;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryBatchOperationHistoryUseCase;
import org.craft.mcs.cap.core.traceability.application.port.out.BatchTraceabilityQueryStore;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceStepType;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Reads post-creation trace facts without exposing the trace graph to material-owning contexts. */
public final class QueryBatchOperationHistoryService implements QueryBatchOperationHistoryUseCase {

  private final @NonNull BatchTraceabilityQueryStore queryStore;

  public QueryBatchOperationHistoryService(@NonNull BatchTraceabilityQueryStore queryStore) {
    this.queryStore = requireNonNull(queryStore, "batch traceability query store cannot be null");
  }

  @Override
  public boolean hasOperations(@NonNull BatchOperationHistoryQuery query) {
    requireNonNull(query, "batch operation history query cannot be null");
    return queryStore.hasOperationStep(
        query.subjectType(),
        query.subjectId(),
        creationStepType(query.subjectType())
    );
  }

  private static @NonNull BatchTraceStepType creationStepType(
      @NonNull BatchTraceSubjectType subjectType
  ) {
    return switch (subjectType) {
      case FEEDSTOCK_BATCH -> BatchTraceStepType.FEEDSTOCK_BATCH_CREATED;
      case PRODUCTION_BATCH -> BatchTraceStepType.PRODUCTION_BATCH_CREATED;
    };
  }
}
