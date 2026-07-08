package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.abv.domain.PotentialAbvPredictor;
import org.craft.mcs.cap.core.production.domain.model.ProductionBatch;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.domain.model.ProductionInput;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.domain.model.ProductionSourceReference;
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
import org.craft.mcs.cap.infrastructure.adapter.in.rest.analysis.abv.mapper.PotentialAbvPredictionRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.CalculatedSugarAcidityAxisPointResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.sugaracidity.dto.SugarAcidityProfilePointResponse;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.YeastAssimilableNitrogenValue;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.measurement.CanonicalUnitLabels;
import org.craft.mcs.cap.shared.infrastructure.rest.measurement.dto.DecimalQuantityResponse;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.kernel.sugar.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.sugar.SugarConcentration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
            ProductionSourceComposition.of(requireNonNull(request.sources(), "production sources cannot be null").stream()
                .map(ProductionBatchRestMapper::toSourceComponent)
                .toList()),
            toProfileSnapshot(requireNonNull(request.profile(), "production profile snapshot cannot be null")),
            toTargetDistance(request.targetDistance())
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
      @NonNull ProductionBatch batch,
      @NonNull PotentialAbvPredictor potentialAbvPredictor
  ) {
    requireNonNull(batch, "production batch cannot be null");
    requireNonNull(potentialAbvPredictor, "potential ABV predictor cannot be null");

    return new ProductionBatchDetailsResponse(
        batch.batchId().value(),
        batch.ciderMakerId().value(),
        batch.name().value(),
        batch.kind(),
        batch.status(),
        new DecimalQuantityResponse(batch.volume().liters(), CanonicalUnitLabels.VOLUME),
        toProfilePointResponse(batch.profile()),
        toYeastAssimilableNitrogenResponse(batch.profile()),
        PotentialAbvPredictionRestMapper.toResponse(potentialAbvPredictor.predict(
            SugarConcentration.ofSpecificGravity(batch.profile().sugar())
        )),
        toTargetDistanceValue(batch.targetDistance()),
        batch.note()
    );
  }

  public static @NonNull ProductionBatchListResponse toListResponse(
      @NonNull List<@NonNull ProductionBatch> batches,
      @NonNull PotentialAbvPredictor potentialAbvPredictor
  ) {
    requireNonNull(batches, "production batches cannot be null");
    requireNonNull(potentialAbvPredictor, "potential ABV predictor cannot be null");

    return new ProductionBatchListResponse(
        batches.stream()
            .map(batch -> ProductionBatchRestMapper.toDetailsResponse(batch, potentialAbvPredictor))
            .toList()
    );
  }

  private static @NonNull ProductionSourceComponent toSourceComponent(
      @NonNull CreateProductionBatchSourceRequest request
  ) {
    requireNonNull(request, "create production batch source request cannot be null");
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(requireNonNull(
            request.feedstockBatchId(),
            "feedstock batch id cannot be null"
        )),
        VolumeValue.ofLiters(requireNonNull(request.volumeLiters(), "production source volume cannot be null"))
    );
  }

  private static @NonNull ProductionProfileSnapshot toProfileSnapshot(
      @NonNull ProductionProfileSnapshotRequest request
  ) {
    requireNonNull(request, "production profile snapshot request cannot be null");
    return new ProductionProfileSnapshot(
        SpecificGravityValue.of(requireNonNull(request.sugar(), "production sugar cannot be null")),
        TitratableAcidityValue.of(requireNonNull(request.acidity(), "production acidity cannot be null")),
        toTanninValue(request),
        toYeastAssimilableNitrogenValue(request)
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
        toYeastAssimilableNitrogenResponse(input.profile()),
        new DecimalQuantityResponse(composition.totalVolume().liters(), CanonicalUnitLabels.VOLUME),
        toTargetDistanceValue(input.targetDistance())
    );
  }

  private static @NonNull ProductionBatchSourceResponse toSourceResponse(
      @NonNull ProductionSourceComponent source
  ) {
    requireNonNull(source, "production source component cannot be null");
    return switch (source.reference().type()) {
      case FEEDSTOCK_BATCH -> new ProductionBatchSourceResponse(
          source.reference().id(),
          new DecimalQuantityResponse(source.volume().liters(), CanonicalUnitLabels.VOLUME)
      );
    };
  }

  private static @NonNull SugarAcidityProfilePointResponse toProfilePointResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    requireNonNull(profile, "production profile snapshot cannot be null");
    return new SugarAcidityProfilePointResponse(
        new CalculatedSugarAcidityAxisPointResponse(profile.sugar().asBigDecimal(), CanonicalUnitLabels.SUGAR),
        new CalculatedSugarAcidityAxisPointResponse(
            profile.acidity().asBigDecimal(),
            CanonicalUnitLabels.TITRATABLE_ACIDITY
        ),
        toTanninAxisPointResponse(profile)
    );
  }

  private static @Nullable ProductionTargetDistance toTargetDistance(@Nullable BigDecimal distance) {
    return distance == null ? null : ProductionTargetDistance.of(distance);
  }

  private static @Nullable BigDecimal toTargetDistanceValue(
      @Nullable ProductionTargetDistance distance
  ) {
    return distance == null ? null : distance.value();
  }

  private static @Nullable TanninityValue toTanninValue(@NonNull ProductionProfileSnapshotRequest request) {
    return request.tannin() == null
           ? null
           : TanninityValue.of(request.tannin());  // NOSONAR
  }

  private static @Nullable YeastAssimilableNitrogenValue toYeastAssimilableNitrogenValue(
      @NonNull ProductionProfileSnapshotRequest request
  ) {
    return request.yeastAssimilableNitrogen() == null
        ? null
        : YeastAssimilableNitrogenValue.of(request.yeastAssimilableNitrogen()); // NOSONAR
  }

  private static @Nullable DecimalQuantityResponse toYeastAssimilableNitrogenResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    final var yan = profile.yeastAssimilableNitrogen();
    return yan == null
        ? null
        : new DecimalQuantityResponse(yan.asBigDecimal(), CanonicalUnitLabels.YEAST_ASSIMILABLE_NITROGEN);
  }

  private static @Nullable CalculatedSugarAcidityAxisPointResponse toTanninAxisPointResponse(
      @NonNull ProductionProfileSnapshot profile
  ) {
    final var tannin = profile.tannin();
    return tannin == null
        ? null
        : new CalculatedSugarAcidityAxisPointResponse(tannin.asBigDecimal(), CanonicalUnitLabels.TANNINITY);
  }
}
