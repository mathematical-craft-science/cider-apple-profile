package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.model;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileSummaryView;
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
    @NonNull FeedstockBatchView batch,
    @Nullable FeedstockCurrentProfileSummaryView currentProfileSummary,
    @NonNull FeedstockCurrentProfileAssessmentView currentProfileAssessment,
    @Nullable FeedstockPlanningTargetCandidateView targetCandidate
) {

  public FeedstockPlanningBatchView {
    requireNonNull(batch, "feedstock batch summary cannot be null");
    requireNonNull(currentProfileAssessment, "feedstock current profile assessment cannot be null");
  }
}
