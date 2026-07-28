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
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.application.compatibility.ChemicalProfileCompositionSourceAdapter;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.ChemicalProfileCompositionOptimizationSource;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjective;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentAdapter;
import org.craft.mcs.cap.core.profileanalysis.compatibility.application.ChemicalProfileAssessmentMatcher;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.CustomRectangleSugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.RectangleSugarAcidityTargetGeometry;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTarget;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.SugarAcidityTargetResolver;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZone;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.InsufficientFeedstockCompositionSourcesException;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.CustomRectangleFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidateFilters;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionCommand;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionResult;
import org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in.OptimizeFeedstockBatchCompositionUseCase;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.PredefinedFeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Builds a composition optimization problem from current feedstock batch measurements.
 *
 * <p>The service projects the feedstock bounded context into the generic profile-analysis optimization
 * model: current batch profiles become optimization sources, batch available volume becomes the
 * source bound, and the selected target is passed to the shared composition optimizer.
 */
@Component
@RequiredArgsConstructor
public final class OptimizeFeedstockBatchCompositionService implements OptimizeFeedstockBatchCompositionUseCase {

  private static final int MINIMUM_SOURCE_COUNT = 2;
  private static final @NonNull String SOURCE_TYPE = "FEEDSTOCK_BATCH";

  private final @NonNull FeedstockCurrentProfileAnalysisCandidateRepository candidateRepository;
  private final @NonNull FeedstockBatchSummaryQueryRepository feedstockBatchQueryRepository;
  private final @NonNull ChemicalProfileAssessmentAdapter chemicalProfileAssessmentAdapter;
  private final @NonNull ChemicalProfileAssessmentMatcher chemicalProfileAssessmentMatcher;
  private final @NonNull SugarAcidityTargetResolver targetResolver;
  private final @NonNull OptimizeChemicalProfileCompositionUseCase optimizeChemicalProfileCompositionUseCase;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;

  @Override
  public @NonNull OptimizeFeedstockBatchCompositionResult optimize(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    requireNonNull(command, "optimize feedstock batch composition command cannot be null");
    feedstockAccessControl.requireReadAccess(command.workspaceId());

    final var batchesById = feedstockBatchQueryRepository.findAllByWorkspaceId(command.workspaceId())
        .stream()
        .collect(Collectors.toMap(FeedstockBatchSummary::batchId, Function.identity()));
    final var sources = candidateRepository.findCandidates(
            new CurrentFeedstockBatchAnalysisCandidateQuery(
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
            command.scoringConfiguration(),
            command.criteria(),
            command.settings(),
            command.mode(),
            command.algorithm(),
            command.calculatorCode(),
            command.targetTotalVolume()
        )
    );

    return new OptimizeFeedstockBatchCompositionResult(sources.size(), optimization);
  }

  private @NonNull Optional<@NonNull ChemicalProfileCompositionOptimizationSource> toOptimizationSourceIfUsable(
      @NonNull FeedstockCurrentProfileView currentProfile,
      @NonNull FeedstockAnalysisCandidateFilters filters,
      @NonNull Map<@NonNull FeedstockBatchId, @NonNull FeedstockBatchSummary> batchesById
  ) {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    final var evaluation = chemicalProfileAssessmentAdapter.project(currentProfile.chemicalProfile());
    if (!evaluation.plottable()) {
      return Optional.empty();
    }
    if (!chemicalProfileAssessmentMatcher.matches(
        evaluation,
        filters.capability(),
        filters.sugarAcidityZone()
    )) {
      return Optional.empty();
    }

    final var assessment = evaluation.assessment();
    if (assessment == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(batchesById.get(currentProfile.batchId()))
        .flatMap(batch -> ChemicalProfileCompositionSourceAdapter.adapt(currentProfile.chemicalProfile())
            .map(characteristics -> new ChemicalProfileCompositionOptimizationSource(
                new ChemicalProfileCompositionSourceReference(
                    SOURCE_TYPE,
                    currentProfile.batchId().value().toString()
                ),
                characteristics,
                batch.availableVolume()
            )));
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

  private @NonNull SugarAcidityTarget target(
      @NonNull OptimizeFeedstockBatchCompositionCommand command
  ) {
    return switch (command.target()) {
      case PredefinedFeedstockProfileTargetSelection(JolicoeurSugarAcidityZone zone) ->
          targetResolver.resolvePredefinedZone(zone);
      case CustomRectangleFeedstockProfileTargetSelection(
          RectangleSugarAcidityTargetGeometry geometry
      ) ->
          new CustomRectangleSugarAcidityTarget(geometry);
    };
  }
}
