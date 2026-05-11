package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.OptimizeChemicalProfileCompositionCommand;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.OptimizeChemicalProfileCompositionUseCase;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionSourceReference;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.ChemicalProfileCompositionTanninPolicy;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.composition.optimization.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.ProfileTargetObjective;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTarget;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessment;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.zone.SugarAcidityZoneAssessor;
import org.craft.mcs.cap.core.feedstock.application.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.application.port.in.CustomRectangleFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.application.port.in.OptimizeFeedstockBatchCompositionUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.in.PredefinedFeedstockBatchBlendProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.port.out.LatestFeedstockMeasurementSugarAcidityPointCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.FeedstockMeasurement;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxisExtractor;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapabilityAssessor;
import org.craft.mcs.cap.shared.platform.query.ChemicalProfileCapabilityCriteria;
import org.craft.mcs.cap.shared.platform.query.SugarAcidityZoneCriteria;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds a composition optimization problem from current feedstock batch measurements.
 *
 * <p>The service projects the feedstock bounded context into the generic cider-analysis optimization
 * model: latest batch measurements become optimization sources, batch available volume becomes the
 * source bound, and the selected target is passed to the shared composition optimizer.
 */
@Component
@RequiredArgsConstructor
public final class OptimizeFeedstockBatchCompositionService implements OptimizeFeedstockBatchCompositionUseCase {

  private static final int MINIMUM_SOURCE_COUNT = 2;
  private static final @NonNull String SOURCE_TYPE = "FEEDSTOCK_BATCH";

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;
  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;
  private final @NonNull ChemicalProfileAxisExtractor axisExtractor;
  private final @NonNull ChemicalProfileCapabilityAssessor capabilityAssessor;
  private final @NonNull SugarAcidityZoneAssessor zoneAssessor;
  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull OptimizeChemicalProfileCompositionUseCase optimizeChemicalProfileCompositionUseCase;

  @Override
  public @NonNull OptimizeFeedstockBatchCompositionResult optimize(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    requireNonNull(command, "optimize feedstock batch composition command cannot be null");

    final var batchesById = feedstockBatchRepository.findAllByCiderMakerId(command.ciderMakerId())
        .stream()
        .collect(Collectors.toMap(FeedstockBatch::batchId, Function.identity()));
    final var sources = feedstockMeasurementQueryRepository.findLatestSugarAcidityPointCandidates(
            new LatestFeedstockMeasurementSugarAcidityPointCandidateQuery(
                command.ciderMakerId(),
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
    final var axes = axisExtractor.extract(measurement.chemicalProfile());
    if (!capabilityAssessor.assess(axes, ChemicalProfileCapability.SUGAR_ACIDITY_PLOT).capable()) {
      return Optional.empty();
    }
    if (!matchesCapability(axes, filters.capability())) {
      return Optional.empty();
    }

    final var assessment = zoneAssessor.assess(axes).orElse(null); // NOSONAR
    if (assessment == null || !matchesSugarAcidityZone(assessment, filters.sugarAcidityZone())) {
      return Optional.empty();
    }

    final var batch = batchesById.get(measurement.batchId());
    if (batch == null) { // NOSONAR
      return Optional.empty();
    }

    return Optional.of(new ChemicalProfileCompositionOptimizationSource(
        new ChemicalProfileCompositionSourceReference(SOURCE_TYPE, measurement.batchId().value().toString()),
        axes,
        batch.availableVolume()
    ));
  }

  private boolean matchesCapability(
      @NonNull ChemicalProfileAxes axes,
      @Nullable ChemicalProfileCapabilityCriteria criteria
  ) {
    if (criteria == null || criteria.capable() == null) {
      return true;
    }
    return capabilityAssessor.assess(axes, criteria.capability()).capable() == criteria.capable(); // NOSONAR
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
    final SugarAcidityTarget target = switch (command.target()) {
      case PredefinedFeedstockBatchBlendProfileTargetSelection predefined -> targetResolver.resolvePredefinedZone(predefined.zone()); // NOSONAR
      case CustomRectangleFeedstockBatchBlendProfileTargetSelection custom -> new CustomRectangleSugarAcidityTarget(custom.geometry()); // NOSONAR
    };

    return command.tanninTarget() == null
        ? ProfileTargetObjective.of(target)
        : ProfileTargetObjective.withTanninTarget(target, command.tanninTarget()); // NOSONAR
  }
}
