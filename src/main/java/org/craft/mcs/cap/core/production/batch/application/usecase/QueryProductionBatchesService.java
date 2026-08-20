package org.craft.mcs.cap.core.production.batch.application.usecase;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.batch.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.batch.application.contract.ProductionBatchView;
import org.craft.mcs.cap.core.production.batch.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.batch.application.port.in.GetProductionBatchUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.in.ListProductionBatchesQuery;
import org.craft.mcs.cap.core.production.batch.application.port.in.ListProductionBatchesUseCase;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.batch.application.port.out.ProductionMeasurementProfileProvider;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionBatchId;
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
  private final @NonNull ProductionMeasurementProfileProvider measurementProfileProvider;
  private final @NonNull ProductionVesselReferenceResolver vesselReferenceResolver;
  private final @NonNull ProductionBatchDetailsAssembler detailsAssembler;

  @Override
  public @NonNull ProductionBatchDetailsView get(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");

    final var batch = productionBatchQueryRepository.findById(batchId)
        .orElseThrow(() -> new ProductionBatchNotFoundException(batchId));
    productionAccessControl.requireReadAccess(batch.workspaceId());
    return detailsAssembler.assemble(batch);
  }

  @Override
  public @NonNull List<@NonNull ProductionBatchDetailsView> list(@NonNull ListProductionBatchesQuery query) {
    requireNonNull(query, "list production batches query cannot be null");
    productionAccessControl.requireReadAccess(query.workspaceId());

    final var batches = productionBatchQueryRepository.findAllByWorkspaceId(query.workspaceId());
    final Set<ProductionBatchId> batchIds = batches.stream()
        .map(ProductionBatchView::batchId)
        .collect(Collectors.toUnmodifiableSet());
    final var measuredProfiles = measurementProfileProvider.findAllCurrent(query.workspaceId(), batchIds);
    final var vessels = vesselReferenceResolver.resolveAll(query.workspaceId(), batches);
    return batches.stream()
        .map(batch -> detailsAssembler.assemble(
            batch,
            requireNonNull(vessels.get(batch.vesselId()), "production vessel reference cannot be null"),
            measuredProfiles.get(batch.batchId())
        ))
        .toList();
  }

}
