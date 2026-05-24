package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.List;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionTargetDistance;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchDetailsResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchListResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionProfileSnapshotRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.CalculatedSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
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
        CiderMakerId.of(requireNonNull(request.ciderMakerId(), "cider maker id cannot be null")),
        ProductionBatchName.of(request.name()),
        requireNonNull(request.kind(), "production batch kind cannot be null"),
        new ProductionInput(
            ProductionSourceComposition.of(request.sources().stream()
                .map(ProductionBatchRestMapper::toSourceComponent)
                .toList()),
            toProfileSnapshot(request.profile()),
            request.targetDistance() == null ? null : ProductionTargetDistance.of(request.targetDistance()) // NOSONAR
        ),
        request.note()
    );
  }

  public static @NonNull ProductionBatchResponse toResponse(@NonNull ProductionBatchCreated created) {
    requireNonNull(created, "created production batch cannot be null");
    final var batch = created.productionBatch();

    return new ProductionBatchResponse(
        batch.batchId().value(),
        batch.ciderMakerId().value(),
        batch.name().value(),
        batch.kind(),
        batch.status(),
        toCompositionResponse(created.input()),
        batch.note()
    );
  }

  public static @NonNull ProductionBatchDetailsResponse toDetailsResponse(
      @NonNull ProductionBatch batch
  ) {
    requireNonNull(batch, "production batch cannot be null");

    return new ProductionBatchDetailsResponse(
        batch.batchId().value(),
        batch.ciderMakerId().value(),
        batch.name().value(),
        batch.kind(),
        batch.status(),
        batch.volume().liters(),
        toProfilePointResponse(batch.profile()),
        batch.targetDistance() == null ? null : batch.targetDistance().value(), // NOSONAR
        batch.note()
    );
  }

  public static @NonNull ProductionBatchListResponse toListResponse(
      @NonNull List<@NonNull ProductionBatch> batches
  ) {
    requireNonNull(batches, "production batches cannot be null");

    return new ProductionBatchListResponse(
        batches.stream()
            .map(ProductionBatchRestMapper::toDetailsResponse)
            .toList()
    );
  }

  private static @NonNull ProductionSourceComponent toSourceComponent(
      @NonNull CreateProductionBatchSourceRequest request
  ) {
    requireNonNull(request, "create production batch source request cannot be null");
    return new ProductionSourceComponent(
        FeedstockBatchId.of(requireNonNull(request.feedstockBatchId(), "feedstock batch id cannot be null")),
        VolumeValue.ofLiters(request.volumeLiters())
    );
  }

  private static @NonNull ProductionProfileSnapshot toProfileSnapshot(
      @NonNull ProductionProfileSnapshotRequest request
  ) {
    requireNonNull(request, "production profile snapshot request cannot be null");
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of(request.sugar()),
        TitratableAcidityValue.of(request.acidity()),
        request.tannin() == null ? null : TanninityValue.of(request.tannin()) // NOSONAR
    );
  }

  private static @NonNull ProductionBatchCompositionResponse toCompositionResponse(
      @NonNull ProductionInput input
  ) {
    requireNonNull(input, "production input cannot be null");
    final var composition = input.sourceComposition();

    return new ProductionBatchCompositionResponse(
        composition.sources().stream()
            .map(ProductionBatchRestMapper::toSourceResponse)
            .toList(),
        toProfilePointResponse(input.profile()),
        composition.totalVolume().liters(),
        input.targetDistance() == null ? null : input.targetDistance().value() // NOSONAR
    );
  }

  private static @NonNull ProductionBatchSourceResponse toSourceResponse(
      @NonNull ProductionSourceComponent source
  ) {
    requireNonNull(source, "production source component cannot be null");
    return new ProductionBatchSourceResponse(
        source.feedstockBatchId().value(),
        source.volume().liters()
    );
  }

  private static @NonNull SugarAcidityProfilePointResponse toProfilePointResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    requireNonNull(profile, "production profile snapshot cannot be null");
    return new SugarAcidityProfilePointResponse(
        new CalculatedSugarAcidityAxisPointResponse(profile.sugar().asBigDecimal()),
        new CalculatedSugarAcidityAxisPointResponse(profile.acidity().asBigDecimal()),
        profile.tannin() == null
            ? null
            : new CalculatedSugarAcidityAxisPointResponse(profile.tannin().asBigDecimal()) // NOSONAR
    );
  }
}
