package org.craft.mcs.cap.core.production.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.application.port.in.GetProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesQuery;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesUseCase;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Read-side application service for production batch queries.
 *
 * <p>The service exposes only production batch state. Source tree, inputs and historical snapshots
 * are read through Batch Traceability endpoints so production DTOs stay focused on the aggregate.
 */
@Component
@RequiredArgsConstructor
public final class QueryProductionBatchesService
    implements GetProductionBatchUseCase, ListProductionBatchesUseCase {

  private final @NonNull ProductionBatchRepository productionBatchRepository;
  private final @NonNull ProductionAccessControl productionAccessControl;

  @Override
  public @NonNull ProductionBatch get(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");

    final var batch = productionBatchRepository.findById(batchId)
        .orElseThrow(() -> new ProductionBatchNotFoundException(batchId));
    productionAccessControl.requireReadAccess(batch.ciderMakerId());
    return batch;
  }

  @Override
  public @NonNull List<@NonNull ProductionBatch> list(@NonNull ListProductionBatchesQuery query) {
    requireNonNull(query, "list production batches query cannot be null");
    productionAccessControl.requireReadAccess(query.ciderMakerId());

    return productionBatchRepository.findAllByCiderMakerId(query.ciderMakerId());
  }
}
