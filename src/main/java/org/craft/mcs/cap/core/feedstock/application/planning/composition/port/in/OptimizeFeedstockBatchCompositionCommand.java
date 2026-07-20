package org.craft.mcs.cap.core.feedstock.application.planning.composition.port.in;

import java.util.List;
import org.craft.mcs.cap.core.profileanalysis.composition.calculation.domain.ChemicalProfileCompositionCalculatorCode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.CompositionOptimizationAlgorithm;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.objective.CompositionOptimizationCriterion;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.CompositionOptimizationMode;
import org.craft.mcs.cap.core.profileanalysis.composition.optimization.domain.strategy.DiscretizedCompositionOptimizationSettings;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.normalization.ProfileAxisNormalizerCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.objective.ProfileTargetObjectiveFunctionCode;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record OptimizeFeedstockBatchCompositionCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockMeasurementPointFilters filters,
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull FeedstockProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull ProfileTargetObjectiveWeights weights,
    @NonNull List<@NonNull CompositionOptimizationCriterion> criteria,
    @NonNull DiscretizedCompositionOptimizationSettings settings,
    @NonNull CompositionOptimizationMode mode,
    @NonNull CompositionOptimizationAlgorithm algorithm,
    @NonNull ChemicalProfileCompositionCalculatorCode calculatorCode,
    @NonNull ProfileAxisNormalizerCode normalizerCode,
    @NonNull ProfileTargetObjectiveFunctionCode objectiveFunctionCode,
    @Nullable VolumeValue targetTotalVolume,
    int sourceLimit
) {

  public OptimizeFeedstockBatchCompositionCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");
    criteria = List.copyOf(requireNonNull(criteria, "composition optimization criteria cannot be null"));
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculatorCode, "chemical profile composition calculator code cannot be null");
    requireNonNull(normalizerCode, "profile axis normalizer code cannot be null");
    requireNonNull(objectiveFunctionCode, "profile target objective function code cannot be null");

    if (sourceLimit < 2 || sourceLimit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "composition optimization source limit must be between 2 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
