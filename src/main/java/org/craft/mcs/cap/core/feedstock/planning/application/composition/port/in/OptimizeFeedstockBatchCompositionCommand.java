package org.craft.mcs.cap.core.feedstock.planning.application.composition.port.in;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Algorithm;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Calculator;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Criterion;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Mode;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Scoring;
import org.craft.mcs.cap.core.feedstock.planning.application.composition.contract.FeedstockCompositionOptimizationRequest.Settings;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphCriteria;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


public record OptimizeFeedstockBatchCompositionCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockPlanningProfileFilters filters,
    @Nullable FeedstockPlanningGraphCriteria sourceZoneCriteria,
    @NonNull FeedstockProfileTargetSelection target,
    @Nullable MeasuredValueRange<TanninValue> tanninTarget,
    @NonNull Scoring scoring,
    @NonNull List<@NonNull Criterion> criteria,
    @NonNull Settings settings,
    @NonNull Mode mode,
    @NonNull Algorithm algorithm,
    @NonNull Calculator calculator,
    @Nullable VolumeValue targetTotalVolume,
    int sourceLimit
) {

  public OptimizeFeedstockBatchCompositionCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
    requireNonNull(scoring, "profile target scoring configuration cannot be null");
    criteria = List.copyOf(requireNonNull(criteria, "composition optimization criteria cannot be null"));
    requireNonNull(settings, "discretized composition optimization settings cannot be null");
    requireNonNull(mode, "composition optimization mode cannot be null");
    requireNonNull(algorithm, "composition optimization algorithm cannot be null");
    requireNonNull(calculator, "composition calculator cannot be null");

    if (sourceLimit < 2 || sourceLimit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "composition optimization source limit must be between 2 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
