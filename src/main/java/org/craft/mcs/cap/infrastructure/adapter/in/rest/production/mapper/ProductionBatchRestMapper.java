package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.List;
import org.craft.mcs.cap.core.production.application.model.ProductionBatchDetailsView;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.candidate.domain.model.ProductionBatchCandidateId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchDetailsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ProductionBatchRestMapper {

  private ProductionBatchRestMapper() {
  }

  public static @NonNull CreateProductionBatchCommand toCommand(
      @NonNull CreateProductionBatchRequest request
  ) {
    requireNonNull(request, "create production batch request cannot be null");

    return new CreateProductionBatchCommand(
        WorkspaceId.of(requireNonNull(request.workspaceId(), "workspace id cannot be null")),
        ProductionBatchName.of(request.name()),
        requireNonNull(request.kind(), "production batch kind cannot be null"),
        ProductionBatchCandidateId.of(requireNonNull(request.candidateId(), "candidate id cannot be null")),
        request.note()
    );
  }

  public static @NonNull ProductionBatchResponse toResponse(@NonNull ProductionBatchCreated created) {
    requireNonNull(created, "created production batch cannot be null");
    final var batch = created.productionBatch();

    return new ProductionBatchResponse(
        batch.batchId().value(),
        batch.workspaceId().value(),
        batch.name().value(),
        batch.kind(),
        batch.status(),
        created.candidate().candidateId().value(),
        ProductionBatchCompositionRestMapper.toResponse(created.candidate()),
        batch.note()
    );
  }

  public static @NonNull ProductionBatchDetailsResponse toDetailsResponse(
      @NonNull ProductionBatchDetailsView details
  ) {
    requireNonNull(details, "production batch details cannot be null");
    final var batch = details.batch();

    return new ProductionBatchDetailsResponse(
        batch.batchId().value(),
        batch.workspaceId().value(),
        batch.name().value(),
        batch.kind(),
        batch.status(),
        new DecimalQuantityResponse(batch.volume().liters(), CanonicalUnitLabels.VOLUME),
        ProductionBatchCompositionRestMapper.toProfilePointResponse(batch.profile()),
        ProductionBatchCompositionRestMapper.toYeastAssimilableNitrogenResponse(batch.profile()),
        PotentialAbvPredictionRestMapper.toResponse(details.potentialAbv()),
        batch.note()
    );
  }

  public static @NonNull ProductionBatchListResponse toListResponse(
      @NonNull List<@NonNull ProductionBatchDetailsView> batches
  ) {
    requireNonNull(batches, "production batches cannot be null");

    return new ProductionBatchListResponse(
        batches.stream()
            .map(ProductionBatchRestMapper::toDetailsResponse)
            .toList()
    );
  }

}
