package org.craft.mcs.cap.core.feedstock.application.planning.composition.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.application.port.in.OptimizeChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileEvaluation;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.projection.domain.SugarAcidityProfileProjector;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.zone.domain.SugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.application.measurement.query.FeedstockBatchCurrentMeasurementQueryService;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds a composition optimization problem from current feedstock batch measurements.
 *
 * <p>The service projects the feedstock bounded context into the generic profile-analysis optimization
 * model: latest batch measurements become optimization sources, batch available volume becomes the
 * source bound, and the selected target is passed to the shared composition optimizer.
 */
@Component
@RequiredArgsConstructor
public final class OptimizeFeedstockBatchCompositionService implements OptimizeFeedstockBatchCompositionUseCase {

  private static final int MINIMUM_SOURCE_COUNT = 2;
  private static final @NonNull String SOURCE_TYPE = "FEEDSTOCK_BATCH";

  private final @NonNull FeedstockBatchCurrentMeasurementQueryService currentMeasurementQueryService;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull SugarAcidityProfileProjector sugarAcidityProfileProjector;
  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull OptimizeChemicalProfileCompositionUseCase optimizeChemicalProfileCompositionUseCase;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;

  @Override
  public @NonNull OptimizeFeedstockBatchCompositionResult optimize(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    requireNonNull(command, "optimize feedstock batch composition command cannot be null");
    feedstockAccessControl.requireReadAccess(command.workspaceId());

    final var batchesById = feedstockBatchRepository.findAllByWorkspaceId(command.workspaceId())
        .stream()
        .collect(Collectors.toMap(FeedstockBatch::batchId, Function.identity()));
    final var sources = currentMeasurementQueryService.findCurrentSugarAcidityPointCandidates(
            new LatestFeedstockMeasurementSugarAcidityPointCandidateQuery(
                command.workspaceId(),
                command.filters().batch(),
                command.filters().season(),
                null,
                command.sourceLimit()
            )
        )
        .stream()
        .map(measurement -> toOptimizationSourceIfUsable(measurement, command.filters(), batchesById))
        .flatMap(Optional::stream)
        .toList();
    if (sources.size() < MINIMUM_SOURCE_COUNT) {
      throw new InsufficientFeedstockCompositionSourcesException(sources.size(), MINIMUM_SOURCE_COUNT);
    }

    final var optimization = optimizeChemicalProfileCompositionUseCase.optimize(
        new OptimizeChemicalProfileCompositionCommand(
            sources,
            command.tanninTarget() == null
                ? ChemicalProfileCompositionTanninPolicy.CALCULATE_WHEN_COMPLETE
                : ChemicalProfileCompositionTanninPolicy.REQUIRE_TANNIN,
            objective(command),
            command.weights(),
            command.purpose(),
            command.criteria(),
            command.settings(),
            command.mode(),
            command.algorithm(),
            command.calculatorCode(),
            command.normalizerCode(),
            command.objectiveFunctionCode(),
            command.targetTotalVolume()
        )
    );

    return new OptimizeFeedstockBatchCompositionResult(sources.size(), optimization);
  }

  private @NonNull Optional<@NonNull ChemicalProfileCompositionOptimizationSource> toOptimizationSourceIfUsable(
      @NonNull FeedstockMeasurement measurement,
      @NonNull FeedstockMeasurementPointFilters filters,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatch> batchesById
  ) {
    requireNonNull(measurement, "feedstock measurement cannot be null");
    final var evaluation = sugarAcidityProfileProjector.project(measurement.chemicalProfile());
    if (!evaluation.plottable()) {
      return Optional.empty();
    }
    if (!matchesCapability(evaluation, filters.capability())) {
      return Optional.empty();
    }

    final var assessment = evaluation.assessment();
    if (assessment == null || !matchesSugarAcidityZone(assessment, filters.sugarAcidityZone())) {
      return Optional.empty();
    }

    return Optional.ofNullable(batchesById.get(measurement.batchId()))
        .map(batch -> new ChemicalProfileCompositionOptimizationSource(
            new ChemicalProfileCompositionSourceReference(SOURCE_TYPE, measurement.batchId().value().toString()),
            evaluation.axes(),
            batch.availableVolume()
        ));
  }

  private boolean matchesCapability(
      @NonNull SugarAcidityProfileEvaluation evaluation,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }
    return sugarAcidityProfileProjector.assessCapability(evaluation, criteria.capability()).capable()
        == Boolean.TRUE.equals(criteria.capable());
  }

  private static boolean matchesSugarAcidityZone(
      @NonNull SugarAcidityZoneAssessment assessment,
      @Nullable SugarAcidityZoneCriteria criteria
  ) {
    return criteria == null || criteria.zones().contains(assessment.zone());
  }

  private @NonNull ProfileTargetObjective objective(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    final SugarAcidityTarget target = target(command);
    final var tanninTarget = command.tanninTarget();
    if (tanninTarget == null) {
      return ProfileTargetObjective.of(target);
    }

    return ProfileTargetObjective.withTanninTarget(target, tanninTarget);
  }

  private @NonNull SugarAcidityTarget target(@NonNull OptimizeFeedstockBatchCompositionCommand command) {
    return switch (command.target()) {
      case PredefinedFeedstockProfileTargetSelection(SugarAcidityZone zone) ->
          targetResolver.resolvePredefinedZone(zone);
      case CustomRectangleFeedstockProfileTargetSelection(RectangleSugarAcidityTargetGeometry geometry) ->
          new CustomRectangleSugarAcidityTarget(geometry);
    };
  }
}
