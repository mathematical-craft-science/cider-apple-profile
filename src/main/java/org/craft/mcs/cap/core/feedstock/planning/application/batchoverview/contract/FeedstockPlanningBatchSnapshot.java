package org.craft.mcs.cap.core.feedstock.planning.application.batchoverview.contract;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/** Planning-owned batch snapshot assembled through the Feedstock batch published boundary. */
public record FeedstockPlanningBatchSnapshot(
    @NonNull FeedstockBatchView batch,
    @Nullable FeedstockCurrentProfileView currentProfile,
    @NonNull FeedstockCurrentProfileAssessmentView assessment
) {

  public FeedstockPlanningBatchSnapshot {
    requireNonNull(batch, "feedstock planning batch cannot be null");
    requireNonNull(assessment, "feedstock planning assessment cannot be null");
  }
}
