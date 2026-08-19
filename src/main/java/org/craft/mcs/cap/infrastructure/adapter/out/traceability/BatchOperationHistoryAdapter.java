package org.craft.mcs.cap.infrastructure.adapter.out.traceability;

import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchOperationHistory;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchOperationHistory;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
import org.craft.mcs.cap.core.traceability.application.port.in.BatchOperationHistoryQuery;
import org.craft.mcs.cap.core.traceability.application.port.in.QueryBatchOperationHistoryUseCase;
import org.craft.mcs.cap.core.traceability.domain.model.BatchTraceSubjectType;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** ACL adapter exposing trace history facts without coupling material contexts to Traceability. */
@Component
@RequiredArgsConstructor
public final class BatchOperationHistoryAdapter
    implements FeedstockBatchOperationHistory, ProductionBatchOperationHistory {

  private final @NonNull QueryBatchOperationHistoryUseCase queryBatchOperationHistoryUseCase;

  @Override
  public boolean hasOperations(@NonNull FeedstockBatchId batchId) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return queryBatchOperationHistoryUseCase.hasOperations(new BatchOperationHistoryQuery(
        BatchTraceSubjectType.FEEDSTOCK_BATCH,
        batchId.value()
    ));
  }

  @Override
  public boolean hasOperations(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");
    return queryBatchOperationHistoryUseCase.hasOperations(new BatchOperationHistoryQuery(
        BatchTraceSubjectType.PRODUCTION_BATCH,
        batchId.value()
    ));
  }
}
