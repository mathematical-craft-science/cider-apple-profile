package org.craft.mcs.cap.core.production.application.usecase;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.production.application.ProductionBatchNotFoundException;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchView;
import org.craft.mcs.cap.core.production.application.model.ProductionMeasurementProfileView;
import org.craft.mcs.cap.core.production.application.authorization.port.out.ProductionAccessControl;
import org.craft.mcs.cap.core.production.application.port.in.GetProductionBatchUseCase;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesQuery;
import org.craft.mcs.cap.core.production.application.port.in.ListProductionBatchesUseCase;
import org.craft.mcs.cap.core.production.application.port.out.ProductionBatchQueryRepository;
import org.craft.mcs.cap.core.production.application.port.out.ProductionMeasurementProfileProvider;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatchId;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
  private final @NonNull ProductionCurrentProfileProjector productionCurrentProfileProjector;
  private final @NonNull ChemicalProfileAxisExtractor chemicalProfileAxisExtractor;
  private final @NonNull PotentialAbvPredictor potentialAbvPredictor;

  @Override
  public @NonNull ProductionBatchDetailsView get(@NonNull ProductionBatchId batchId) {
    requireNonNull(batchId, "production batch id cannot be null");

    final var batch = productionBatchQueryRepository.findById(batchId)
        .orElseThrow(() -> new ProductionBatchNotFoundException(batchId));
    productionAccessControl.requireReadAccess(batch.workspaceId());
    return toDetailsView(batch, measurementProfileProvider.findCurrent(batch.batchId()).orElse(null)); // NOSONAR
  }

  @Override
  public @NonNull List<@NonNull ProductionBatchDetailsView> list(@NonNull ListProductionBatchesQuery query) {
    requireNonNull(query, "list production batches query cannot be null");
    productionAccessControl.requireReadAccess(query.workspaceId());

    final var batches = productionBatchQueryRepository.findAllByWorkspaceId(query.workspaceId());
    final Set<ProductionBatchId> batchIds = batches.stream()
        .map(ProductionBatchView::batchId)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
    final var measuredProfiles = measurementProfileProvider.findAllCurrent(query.workspaceId(), batchIds);

    return batches.stream()
        .map(batch -> toDetailsView(batch, measuredProfiles.get(batch.batchId())))
        .toList();
  }

  private @NonNull ProductionBatchDetailsView toDetailsView(
      @NonNull ProductionBatchView batch,
      @Nullable ProductionMeasurementProfileView measuredProfile
  ) {
    final var currentProfile = productionCurrentProfileProjector.project(
        batch.initialProfileSnapshot(),
        measuredProfile
    );
    return new ProductionBatchDetailsView(
        batch,
        currentProfile,
        potentialAbvPredictor.predict(
            SpecificGravityValue.of(
                chemicalProfileAxisExtractor.extract(currentProfile.chemicalProfile())
                    .requireValueOf(ChemicalAxis.SUGAR)
                    .value()
            )
        )
    );
  }

}
