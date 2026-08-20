package org.craft.mcs.cap.infrastructure.adapter.in.rest.production.mapper;

import java.util.UUID;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest.TargetDefinition;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest.Weights;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateNormalizer;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateView;
import org.craft.mcs.cap.core.production.planning.application.port.in.CreateProductionBatchCandidateCommand;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComponent;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceComposition;
import org.craft.mcs.cap.core.production.batch.domain.model.ProductionSourceReference;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionCalculatorCode;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.dto.CompositionTanninPolicy;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.CreateProductionBatchCandidateRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCandidateResponse;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.production.dto.ProductionBatchCandidateSourceRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.composition.mapper.CompositionPlanningRestDefaults;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.CustomRectangleProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.PredefinedZoneProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetTanninRangeRequest;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.shared.profiletarget.dto.ProfileTargetObjectiveWeightsRequest;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Maps production-candidate transport data while resolving target specifications at the REST boundary. */
@Component
public final class ProductionBatchCandidateRestMapper {

  public @NonNull CreateProductionBatchCandidateCommand toCommand(
      @NonNull CreateProductionBatchCandidateRequest request
  ) {
    requireNonNull(request, "create production batch candidate request cannot be null");
    final var sources = requireNonNull(request.sources(), "production candidate sources cannot be null")
        .stream()
        .map(ProductionBatchCandidateRestMapper::toSourceComponent)
        .toList();
    final var requestedObjectiveFunction = request.objectiveFunction();
    final var objectiveFunction = requestedObjectiveFunction == null
        ? CompositionPlanningRestDefaults.profileTargetObjectiveFunction()
        : requestedObjectiveFunction;
    final var requestedTanninPolicy = request.tanninPolicy();
    final var tanninPolicy = requestedTanninPolicy == null
        ? CompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
        : requestedTanninPolicy;
    final var requestedNormalizer = request.normalizer();
    final var normalizer = requestedNormalizer == null
        ? CompositionPlanningRestDefaults.profileTargetNormalizer()
        : requestedNormalizer;
    final var requestedCalculator = request.calculator();
    final var calculator = requestedCalculator == null
        ? CompositionCalculatorCode.MASS_BALANCE
        : requestedCalculator;

    return new CreateProductionBatchCandidateCommand(
        WorkspaceId.of(requireNonNull(request.workspaceId(), "workspace id cannot be null")),
        ProductionSourceComposition.of(sources),
        new ProductionBatchCandidateCalculationRequest(
            ProductionBatchCandidateCalculationRequest.TanninPolicy.valueOf(tanninPolicy.name()),
            toTarget(
                request.target(),
                request.tanninTarget(),
                normalizer,
                objectiveFunction,
                request.weights()
            ),
            ProductionBatchCandidateCalculationRequest.Calculator.valueOf(calculator.name())
        )
    );
  }

  private static @Nullable TargetDefinition toTarget(
      @Nullable ProfileTargetRequest target,
      @Nullable ProfileTargetTanninRangeRequest tannin,
      @NonNull ProfileAxisNormalizerCode normalizer,
      @NonNull ProfileTargetObjectiveFunctionCode objectiveFunction,
      @Nullable ProfileTargetObjectiveWeightsRequest weights
  ) {
    if (target == null) {
      return null;
    }
    return new ProductionBatchCandidateCalculationRequest.TargetDefinition(
        switch (target) {
          case PredefinedZoneProfileTargetRequest(_, var zone) ->
              new ProductionBatchCandidateCalculationRequest.PredefinedTarget(
                  ProductionBatchCandidateCalculationRequest.Zone.valueOf(zone.name())
              );
          case CustomRectangleProfileTargetRequest rectangle ->
              new ProductionBatchCandidateCalculationRequest.CustomRectangleTarget(
                  MeasuredValueRange.closed(
                      SpecificGravityValue.of(rectangle.specificGravityMin()),
                      SpecificGravityValue.of(rectangle.specificGravityMax())
                  ),
                  MeasuredValueRange.closed(
                      TitratableAcidityValue.of(rectangle.titratableAcidityMin()),
                      TitratableAcidityValue.of(rectangle.titratableAcidityMax())
                  )
              );
        },
        tannin == null
            ? null
            : MeasuredValueRange.closed(TanninValue.of(tannin.min()), TanninValue.of(tannin.max())),
        new ProductionBatchCandidateCalculationRequest.Scoring(
            ProductionCandidateNormalizer.valueOf(normalizer.name()),
            ProductionBatchCandidateCalculationRequest.ObjectiveFunction.valueOf(objectiveFunction.name()),
            toWeights(weights)
        )
    );
  }

  private static @NonNull Weights toWeights(
      @Nullable ProfileTargetObjectiveWeightsRequest weights
  ) {
    if (weights == null) {
      return new ProductionBatchCandidateCalculationRequest.Weights(
          java.math.BigDecimal.ONE,
          java.math.BigDecimal.ONE,
          null
      );
    }
    final var specificGravity = weights.specificGravity();
    final var titratableAcidity = weights.titratableAcidity();
    return new ProductionBatchCandidateCalculationRequest.Weights(
        specificGravity == null ? java.math.BigDecimal.ONE : specificGravity,
        titratableAcidity == null ? java.math.BigDecimal.ONE : titratableAcidity,
        weights.tannin()
    );
  }

  public static @NonNull ProductionBatchCandidateResponse toResponse(
      @NonNull ProductionBatchCandidateView candidate
  ) {
    requireNonNull(candidate, "production batch candidate cannot be null");
    final var confirmedProductionBatchId = candidate.confirmedProductionBatchId();
    return new ProductionBatchCandidateResponse(
        candidate.candidateId().value(),
        candidate.workspaceId().value(),
        candidate.status(),
        ProductionBatchCompositionRestMapper.toResponse(candidate),
        confirmedProductionBatchId == null
            ? null
            : confirmedProductionBatchId.value()
    );
  }

  private static @NonNull ProductionSourceComponent toSourceComponent(
      @NonNull ProductionBatchCandidateSourceRequest request
  ) {
    requireNonNull(request, "create production batch source request cannot be null");
    final UUID feedstockBatchId = requireNonNull(
        request.feedstockBatchId(),
        "feedstock batch id cannot be null"
    );
    return new ProductionSourceComponent(
        ProductionSourceReference.feedstockBatch(feedstockBatchId),
        VolumeValue.ofLiters(requireNonNull(request.volumeLiters(), "production source volume cannot be null"))
    );
  }
}
