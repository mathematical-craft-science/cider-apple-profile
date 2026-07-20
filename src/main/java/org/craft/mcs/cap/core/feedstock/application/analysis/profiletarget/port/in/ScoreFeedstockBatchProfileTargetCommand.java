package org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in;

import org.craft.mcs.cap.shared.application.ApplicationValidationException;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.analysis.sugaracidity.port.in.FeedstockMeasurementPointFilters;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.application.query.QueryLimits;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ScoreFeedstockBatchProfileTargetCommand(
    @NonNull WorkspaceId workspaceId,
    @NonNull FeedstockMeasurementPointFilters filters,
    @NonNull ProfileTargetScoringPurpose purpose,
    @NonNull FeedstockProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull ProfileTargetObjectiveWeights weights,
    int limit
) {

  public ScoreFeedstockBatchProfileTargetCommand {
    requireNonNull(workspaceId, "workspace id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch profile target cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");

    if (limit < 1 || limit > QueryLimits.MAXIMUM) {
      throw new ApplicationValidationException(
          "profile target score limit must be between 1 and " + QueryLimits.MAXIMUM
      );
    }
  }
}
