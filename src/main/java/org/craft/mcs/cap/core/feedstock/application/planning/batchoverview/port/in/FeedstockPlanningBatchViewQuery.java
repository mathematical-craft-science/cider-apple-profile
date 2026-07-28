package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.model.ProfileTargetAnalysisScenario;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockPlanningBatchViewQuery(
    @NonNull WorkspaceId workspaceId,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull ProfileTargetObjectiveWeights weights,
    @NonNull ProfileTargetAnalysisScenario analysisScenario,
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
