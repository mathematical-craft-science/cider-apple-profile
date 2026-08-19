package org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.CompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationCriterion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetScoringConfiguration;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record OptimizeFeedstockBatchCompositionCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockPlanningProfileFilters filters,
    @Nullable JolicoeurSugarAcidityZoneCriteria sourceZoneCriteria,
    @NonNull FeedstockProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull ProfileTargetScoringConfiguration scoringConfiguration,
    @NonNull List<@NonNull CompositionOptimizationCriterion> criteria,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull CompositionCalculatorCode calculatorCode,
    @Nullable VolumeValue targetTotalVolume,
    int sourceLimit
) {

  public OptimizeFeedstockBatchCompositionCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
    requireNonNull(scoringConfiguration, "profile target scoring configuration cannot be null");
    criteria = List.copyOf(requireNonNull(criteria, "composition optimization criteria cannot be null"));
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "composition calculator code cannot be null");

    if (sourceLimit < 2 || sourceLimit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "composition optimization source limit must be between 2 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
