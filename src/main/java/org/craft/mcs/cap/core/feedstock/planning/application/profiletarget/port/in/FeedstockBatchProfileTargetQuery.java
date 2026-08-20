package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import static java.util.Objects.requireNonNull;

import org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.feedstock.planning.application.graph.contract.FeedstockPlanningGraphCriteria;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.AnalysisScenario;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.contract.FeedstockTargetFitConfiguration.ObjectiveWeights;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.chemical.value.TanninValue;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.MeasuredValueRange;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Shared query criteria for feedstock profile-target scoring and its enriched candidate projection.
 */
public record FeedstockBatchProfileTargetQuery(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockPlanningProfileFilters filters,
    @Nullable FeedstockPlanningGraphCriteria sourceZoneCriteria,
    @NonNull AnalysisScenario analysisScenario,
    @NonNull FeedstockProfileTargetSelection target,
    @Nullable MeasuredValueRange<TanninValue> tanninTarget,
    @NonNull ObjectiveWeights weights,
    int limit
) {

  public FeedstockBatchProfileTargetQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock analysis candidate filters cannot be null");
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");

    if (limit < 1 || limit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "profile target query limit must be between 1 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
