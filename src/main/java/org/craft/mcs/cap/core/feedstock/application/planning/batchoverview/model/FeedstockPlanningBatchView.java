package org.craft.mcs.cap.core.feedstock.application.planning.batchoverview.model;

import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileSummaryView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Batch row in the feedstock planning read model.
 *
 * <p>The view keeps the batch as the primary entity and attaches composite current-profile
 * analysis, planning diagnostics and optional target assessment without changing aggregate state.
 */
public record FeedstockPlanningBatchView(
    @NonNull FeedstockBatchSummary batch,
    @Nullable FeedstockCurrentProfileSummaryView currentProfileSummary,
    @NonNull FeedstockCurrentProfileAssessmentView currentProfileAssessment,
    @Nullable FeedstockPlanningTargetCandidateView targetCandidate
) {

  public FeedstockPlanningBatchView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(currentProfileAssessment, "feedstock current profile assessment cannot be null");
  }
}
