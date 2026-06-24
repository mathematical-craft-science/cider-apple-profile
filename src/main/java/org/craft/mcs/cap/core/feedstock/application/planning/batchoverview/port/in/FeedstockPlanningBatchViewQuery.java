package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.port.in;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetScoringPurpose;
import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.domain.ProfileTargetObjectiveWeights;
import org.craft.mcs.cap.core.profileanalysis.sugaracidity.target.domain.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.application.analysis.profiletarget.port.in.FeedstockProfileTargetSelection;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockPlanningBatchViewQuery(
    @NonNull CiderMakerId ciderMakerId,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull ProfileTargetObjectiveWeights weights,
    @NonNull ProfileTargetScoringPurpose purpose,
    int targetCandidateLimit
) {

  public FeedstockPlanningBatchViewQuery {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(weights, "profile target objective weights cannot be null");
    requireNonNull(purpose, "profile target scoring purpose cannot be null");
    if (targetCandidateLimit < 1 || targetCandidateLimit > CursorPageRequest.MAX_LIMIT) {
      throw new ApplicationValidationException(
          "feedstock planning target candidate limit must be between 1 and " + CursorPageRequest.MAX_LIMIT
      );
    }
  }
}
