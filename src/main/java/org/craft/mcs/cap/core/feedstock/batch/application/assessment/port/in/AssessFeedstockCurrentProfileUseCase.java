package org.craft.mcs.cap.core.feedstock.batch.application.assessment.port.in;

import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.assessment.contract.FeedstockCurrentProfileAssessmentView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Publishes the single-batch assessment of a coherent current Feedstock profile. */
public interface AssessFeedstockCurrentProfileUseCase {

  @NonNull
  FeedstockCurrentProfileAssessmentView assess(
      @NonNull FeedstockBatchSummary batch,
      @Nullable FeedstockCurrentProfileView currentProfile
  );
}
