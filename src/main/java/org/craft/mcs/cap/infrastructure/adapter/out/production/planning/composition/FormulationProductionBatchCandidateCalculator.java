package org.craft.mcs.cap.infrastructure.adapter.out.production.planning.composition;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.production.planning.application.contract.AvailableProductionSource;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculation;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest.TargetArea;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionBatchCandidateCalculationRequest.TargetDefinition;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateNormalizer;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidatePotentialAbvView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateTargetDistanceView;
import org.craft.mcs.cap.core.production.planning.application.contract.ProductionCandidateWarning;
import org.craft.mcs.cap.core.production.planning.application.InvalidProductionCandidateCalculationException;
import org.craft.mcs.cap.core.production.planning.application.ProductionCandidateTargetFitUnavailableException;
import org.craft.mcs.cap.core.production.planning.application.UnsupportedProductionCandidateCalculationException;
import org.craft.mcs.cap.core.production.planning.application.port.out.ProductionBatchCandidateCalculator;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionSourceCharacteristics;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculatedCompositionView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CalculateCompositionResult;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.CompositionSourceInput;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationCalculator;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTanninPolicy;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationPotentialAbvView;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDefinition;
import org.craft.mcs.cap.core.formulation.calculation.application.contract.FormulationTargetDistanceView;
import org.craft.mcs.cap.core.formulation.calculation.application.FormulationTargetFitUnavailableException;
import org.craft.mcs.cap.core.formulation.calculation.application.InvalidCompositionRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.UnsupportedCompositionCalculatorRequestException;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionCommand;
import org.craft.mcs.cap.core.formulation.calculation.application.port.in.CalculateCompositionUseCase;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Adapts authoritative production source snapshots to the Formulation calculation contract. */
@Component
@RequiredArgsConstructor
public final class FormulationProductionBatchCandidateCalculator
    implements ProductionBatchCandidateCalculator {

  private final @NonNull CalculateCompositionUseCase calculateCompositionUseCase;

  @Override
  public @NonNull ProductionBatchCandidateCalculation calculate(
      @NonNull List<@NonNull AvailableProductionSource> sources,
      @NonNull ProductionBatchCandidateCalculationRequest request
  ) {
    requireNonNull(sources, "available production sources cannot be null");
    requireNonNull(request, "production batch candidate calculation request cannot be null");
    final var result = calculateFormulation(sources, request);
    final var normalizerCode = result.normalizerCode();
    return new ProductionBatchCandidateCalculation(
        toProductionProfile(result.composition()),
        toPotentialAbv(result.potentialAbv()),
        normalizerCode == null
            ? null
            : ProductionCandidateNormalizer.valueOf(normalizerCode.name()),
        toTargetDistance(result.targetDistance()),
        result.composition().warnings().stream()
            .map(warning -> ProductionCandidateWarning.valueOf(warning.name()))
            .toList()
    );
  }

  private @NonNull CalculateCompositionResult calculateFormulation(
      @NonNull List<@NonNull AvailableProductionSource> sources,
      @NonNull ProductionBatchCandidateCalculationRequest request
  ) {
    try {
      return calculateCompositionUseCase.calculate(new CalculateCompositionCommand(
          sources.stream()
              .map(FormulationProductionBatchCandidateCalculator::toCompositionSource)
              .toList(),
          FormulationTanninPolicy.valueOf(request.tanninPolicy().name()),
          toTarget(request.target()),
          FormulationCalculator.valueOf(request.calculator().name())
      ));
    } catch (InvalidCompositionRequestException exception) {
      throw new InvalidProductionCandidateCalculationException(exception.getMessage(), exception);
    } catch (UnsupportedCompositionCalculatorRequestException exception) {
      throw new UnsupportedProductionCandidateCalculationException(exception.getMessage(), exception);
    } catch (FormulationTargetFitUnavailableException exception) {
      throw new ProductionCandidateTargetFitUnavailableException(exception.getMessage(), exception);
    }
  }

  private static @NonNull CompositionSourceInput toCompositionSource(
      @NonNull AvailableProductionSource source
  ) {
    return new CompositionSourceInput(
        new CompositionSourceInput.Reference(
            source.reference().type().name(),
            source.reference().id().toString()
        ),
        toCompositionCharacteristics(source.characteristics()),
        new CompositionSourceInput.AbsoluteVolume(source.selectedVolume()),
        source.availableVolume()
    );
  }

  private static CompositionSourceInput.@NonNull Characteristics toCompositionCharacteristics(
      @NonNull ProductionSourceCharacteristics characteristics
  ) {
    return new CompositionSourceInput.Characteristics(
        characteristics.specificGravity(),
        characteristics.titratableAcidity(),
        characteristics.tannin(),
        characteristics.yeastAssimilableNitrogen()
    );
  }

  private static @NonNull ProductionProfileSnapshot toProductionProfile(
      @NonNull CalculatedCompositionView composition
  ) {
    final var characteristics = composition.characteristics();
    return new ProductionProfileSnapshot(
        characteristics.specificGravity(),
        characteristics.titratableAcidity(),
        characteristics.tannin(),
        characteristics.yeastAssimilableNitrogen()
    );
  }

  private static @Nullable FormulationTargetDefinition toTarget(
      @Nullable TargetDefinition target
  ) {
    if (target == null) {
      return null;
    }
    final var scoring = target.scoring();
    return new FormulationTargetDefinition(
        toTargetArea(target.sugarAcidity()),
        target.tannin(),
        new FormulationTargetDefinition.ScoringConfiguration(
            FormulationTargetDefinition.Normalizer.valueOf(scoring.normalizer().name()),
            FormulationTargetDefinition.ObjectiveFunction.valueOf(scoring.objectiveFunction().name()),
            new FormulationTargetDefinition.Weights(
                scoring.weights().specificGravity(),
                scoring.weights().titratableAcidity(),
                scoring.weights().tannin()
            )
        )
    );
  }

  private static FormulationTargetDefinition.@NonNull TargetArea toTargetArea(
      @NonNull TargetArea target
  ) {
    return switch (target) {
      case ProductionBatchCandidateCalculationRequest.PredefinedTarget(var zone) ->
          new FormulationTargetDefinition.PredefinedTarget(
              FormulationTargetDefinition.Zone.valueOf(zone.name())
          );
      case ProductionBatchCandidateCalculationRequest.CustomRectangleTarget(
          var specificGravity,
          var titratableAcidity
      ) -> new FormulationTargetDefinition.CustomRectangleTarget(specificGravity, titratableAcidity);
    };
  }

  private static @NonNull ProductionCandidatePotentialAbvView toPotentialAbv(
      @NonNull FormulationPotentialAbvView potentialAbv
  ) {
    return new ProductionCandidatePotentialAbvView(
        potentialAbv.originalGravity(),
        potentialAbv.predictedFinalGravity(),
        ProductionCandidatePotentialAbvView.FinalGravitySource.valueOf(
            potentialAbv.predictedFinalGravitySource().name()
        ),
        potentialAbv.potentialAbv(),
        ProductionCandidatePotentialAbvView.Method.valueOf(potentialAbv.method().name()),
        ProductionCandidatePotentialAbvView.Confidence.valueOf(potentialAbv.confidence().name())
    );
  }

  private static @Nullable ProductionCandidateTargetDistanceView toTargetDistance(
      @Nullable FormulationTargetDistanceView distance
  ) {
    if (distance == null) {
      return null;
    }
    return new ProductionCandidateTargetDistanceView(
        ProductionCandidateTargetDistanceView.Status.valueOf(distance.status().name()),
        toCoordinates(distance.point()),
        toCoordinates(distance.nearestTargetPoint()),
        new ProductionCandidateTargetDistanceView.TargetPosition(
            ProductionCandidateTargetDistanceView.AxisPosition.valueOf(
                distance.targetPosition().specificGravity().name()
            ),
            ProductionCandidateTargetDistanceView.AxisPosition.valueOf(
                distance.targetPosition().titratableAcidity().name()
            )
        ),
        distance.tannin(),
        distance.nearestTargetTannin(),
        distance.value(),
        distance.matchPercent(),
        ProductionCandidateTargetDistanceView.FitStatus.valueOf(distance.fitStatus().name()),
        distance.insideSugarAcidityTarget(),
        distance.insideTarget(),
        ProductionBatchCandidateCalculationRequest.ObjectiveFunction.valueOf(distance.objectiveFunction().name()),
        ProductionCandidateTargetDistanceView.Metric.valueOf(distance.metric().name()),
        new ProductionCandidateTargetDistanceView.Weights(
            distance.weights().specificGravity(),
            distance.weights().titratableAcidity(),
            distance.weights().tannin()
        ),
        ProductionCandidateTargetDistanceView.TargetKind.valueOf(distance.targetKind().name()),
        ProductionCandidateTargetDistanceView.GeometryShape.valueOf(distance.targetShape().name()),
        distance.missingAxes().stream()
            .map(axis -> ProductionCandidateTargetDistanceView.EvaluationAxis.valueOf(axis.name()))
            .collect(java.util.stream.Collectors.toUnmodifiableSet())
    );
  }

  private static ProductionCandidateTargetDistanceView.@NonNull Coordinates toCoordinates(
      FormulationTargetDistanceView.@NonNull Coordinates coordinates
  ) {
    return new ProductionCandidateTargetDistanceView.Coordinates(
        coordinates.specificGravity(),
        coordinates.titratableAcidity()
    );
  }
}
