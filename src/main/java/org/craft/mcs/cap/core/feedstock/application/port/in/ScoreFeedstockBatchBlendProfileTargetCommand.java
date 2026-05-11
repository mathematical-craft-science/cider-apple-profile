package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.cideranalysis.profileevaluation.application.port.in.BlendProfileScoringPurpose;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.distance.BlendProfileObjectiveWeights;
import org.craft.mcs.cap.core.cideranalysis.profileevaluation.domain.target.TanninTargetRange;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ScoreFeedstockBatchBlendProfileTargetCommand(
    @NonNull CiderMakerId ciderMakerId,
    @NonNull FeedstockMeasurementPointFilters filters,
    @NonNull BlendProfileScoringPurpose purpose,
    @NonNull FeedstockBatchBlendProfileTargetSelection target,
    @Nullable TanninTargetRange tanninTarget,
    @NonNull BlendProfileObjectiveWeights weights,
    int limit
) {

  public ScoreFeedstockBatchBlendProfileTargetCommand {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
    requireNonNull(filters, "feedstock measurement point filters cannot be null");
    requireNonNull(purpose, "blend profile scoring purpose cannot be null");
    requireNonNull(target, "feedstock batch blend profile target cannot be null");
    requireNonNull(weights, "blend profile objective weights cannot be null");

    if (limit < 1 || limit > CursorPageRequest.MAX_LIMIT) {
      throw new IllegalArgumentException("blend profile score limit must be between 1 and " + CursorPageRequest.MAX_LIMIT);
    }
  }
}
