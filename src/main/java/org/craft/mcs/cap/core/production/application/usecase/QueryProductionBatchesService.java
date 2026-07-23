package org.craft.mcs.cap.core.production.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.application.port.in.GetProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesQuery;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesUseCase;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
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

  private final @NonNull ProductionBatchQueryRepository productionBatchQueryRepository;
  private final @NonNull ProductionAccessControl productionAccessControl;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull ProductionBatchDetailsView get(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");

    final var batch = productionBatchQueryRepository.findById(batchId)
        .orElseThrow(() -> new ProductionBatchNotFoundException(batchId));
    productionAccessControl.requireReadAccess(batch.workspaceId());
    return toDetailsView(batch);
  }

  @Override
  public @NonNull List<@NonNull ProductionBatchDetailsView> list(@NonNull ListProductionBatchesQuery query) {
    requireNonNull(query, "list production batches query cannot be null");
    productionAccessControl.requireReadAccess(query.workspaceId());

    return productionBatchQueryRepository.findAllByWorkspaceId(query.workspaceId()).stream()
        .map(this::toDetailsView)
        .toList();
  }

  private @NonNull ProductionBatchDetailsView toDetailsView(
      @NonNull ProductionBatchView batch
  ) {
    return new ProductionBatchDetailsView(
        batch,
        potentialAbvPredictor.predict(
            SugarConcentration.ofSpecificGravity(batch.initialProfileSnapshot().sugar())
        )
    );
  }
}
