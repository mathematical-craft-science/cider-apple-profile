package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.in;

import org.craft.mcs.cap.core.feedstock.planning.application.candidate.model.FeedstockPlanningProfileFilters;
import org.craft.mcs.cap.core.profileanalysis.graph.application.model.JolicoeurSugarAcidityZoneCriteria;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Shared query criteria for feedstock profile-target scoring and its enriched candidate projection. */
public record FeedstockBatchProfileTargetQuery(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockPlanningProfileFilters filters,
    @Nullable JolicoeurSugarAcidityZoneCriteria sourceZoneCriteria,
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
    @NonNull FeedstockProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull ProfileTargetObjectiveWeights weights,
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
