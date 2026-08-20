package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.port.in;

import static java.util.Objects.requireNonNull;

import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
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


public record FeedstockPlanningBatchViewQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockProfileTargetSelection target,
    @Nullable MeasuredValueRange<TanninValue> tanninTarget,
    @NonNull ObjectiveWeights weights,
    @NonNull AnalysisScenario analysisScenario,
    int targetCandidateLimit
) {

  public FeedstockPlanningBatchViewQuery {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");
    requireNonNull(analysisScenario, "profile target analysis scenario cannot be null");
    if (targetCandidateLimit < 1 || targetCandidateLimit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "feedstock planning target candidate limit must be between 1 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
