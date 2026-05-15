package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import org.craft.mcs.cap.core.cideranalysis.composition.calculation.application.port.in.CalculateChemicalProfileCompositionResult;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionSource;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.composition.calculation.domain.AbsoluteCompositionVolume;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.cideranalysis.profiletarget.scoring.domain.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchCommand;
import org.craft.mcs.cap.core.production.application.port.in.CreateProductionBatchSourceCommand;
import org.craft.mcs.cap.core.production.application.port.in.ProductionBatchCreated;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCompositionResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchSourceResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.mapper.ProfileTargetRestMapper;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.sugaracidity.mapper.SugarAcidityZoneRestMapper;
import org.craft.mcs.cap.core.cideranalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public final class ProductionBatchRestMapper {

  private ProductionBatchRestMapper() {
  }

  public static @NonNull CreateProductionBatchCommand toCommand(
      @NonNull CreateProductionBatchRequest request,
      @NonNull SugarAcidityTargetResolver targetResolver
  ) {
    requireNonNull(request, "create production batch request cannot be null");
    requireNonNull(targetResolver, "sugar-acidity target resolver cannot be null");

    final ProfileTargetScoringPurpose scoringPurpose = request.scoringPurpose() == null
        ? ProfileTargetScoringPurpose.BLENDING_PLANNING
        : request.scoringPurpose();
    final ChemicalProfileCompositionTanninPolicy tanninPolicy = request.tanninPolicy() == null
        ? defaultTanninPolicy(request)
        : request.tanninPolicy();
    final ChemicalProfileCompositionCalculatorCode calculator = request.calculator() == null
        ? ChemicalProfileCompositionCalculatorCode.MASS_BALANCE
        : request.calculator();
    final ProfileAxisNormalizerCode normalizer = request.normalizer() == null
        ? ProfileTargetRestMapper.normalizerFor(scoringPurpose)
        : request.normalizer();
    final ProfileTargetObjectiveFunctionCode objectiveFunction = request.objectiveFunction() == null
        ? ProfileTargetObjectiveFunctionCode.DISTANCE_TO_TARGET_AREA
        : request.objectiveFunction();

    return new CreateProductionBatchCommand( // NOSONAR
        CiderMakerId.of(requireNonNull(request.ciderMakerId(), "cider maker id cannot be null")),
        ProductionBatchName.of(request.name()),
        requireNonNull(request.kind(), "production batch kind cannot be null"),
        request.sources().stream()
            .map(ProductionBatchRestMapper::toSourceCommand)
            .toList(),
        tanninPolicy,
        ProfileTargetRestMapper.toTargetObjective(request.target(), request.tanninTarget(), targetResolver),
        ProfileTargetRestMapper.toWeights(request.weights()),
        scoringPurpose,
        calculator,
        normalizer,
        objectiveFunction,
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
        toCompositionResponse(created.composition()),
        batch.note()
    );
  }

  private static @NonNull CreateProductionBatchSourceCommand toSourceCommand(
      @NonNull CreateProductionBatchSourceRequest request
  ) {
    requireNonNull(request, "create production batch source request cannot be null");
    return new CreateProductionBatchSourceCommand(
        FeedstockBatchId.of(requireNonNull(request.feedstockBatchId(), "feedstock batch id cannot be null")),
        VolumeValue.ofLiters(request.volumeLiters())
    );
  }

  private static @NonNull ProductionBatchCompositionResponse toCompositionResponse(
      @NonNull CalculateChemicalProfileCompositionResult result
  ) {
    requireNonNull(result, "production composition result cannot be null");
    final var composition = result.composition();

    return new ProductionBatchCompositionResponse(
        composition.sources().stream()
            .map(ProductionBatchRestMapper::toSourceResponse)
            .toList(),
        SugarAcidityZoneRestMapper.toResponse(composition.profile()),
        requireNonNull(composition.totalVolume(), "production composition total volume cannot be null").liters(),
        composition.warnings(),
        result.normalizerCode(),
        result.targetDistance() == null ? null : ProfileTargetRestMapper.toResponse(result.targetDistance()) // NOSONAR
    );
  }

  private static @NonNull ProductionBatchSourceResponse toSourceResponse(
      @NonNull ChemicalProfileCompositionSource source
  ) {
    requireNonNull(source, "production composition source cannot be null");

    if (!(source.weight() instanceof AbsoluteCompositionVolume(VolumeValue volume))) {
      throw new IllegalStateException("production composition source must use absolute volume");
    }

    return new ProductionBatchSourceResponse(
        java.util.UUID.fromString(source.reference().id()),
        volume.liters()
    );
  }

  private static @NonNull ChemicalProfileCompositionTanninPolicy defaultTanninPolicy(
      @NonNull CreateProductionBatchRequest request
  ) {
    return request.tanninTarget() == null
        ? ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
        : ChemicalProfileCompositionTanninPolicy.REQUIRE_TANNIN;
  }
}
