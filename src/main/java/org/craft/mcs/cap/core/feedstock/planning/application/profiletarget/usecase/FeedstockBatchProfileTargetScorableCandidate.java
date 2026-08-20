package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase;

import static java.util.Objects.requireNonNull;

import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.port.out.FeedstockTargetFitAnalyzer.Candidate;
import org.jspecify.annotations.NonNull;


record FeedstockBatchProfileTargetScorableCandidate(
    @NonNull FeedstockCurrentProfileView currentProfile,
    @NonNull Candidate candidate
) {

  FeedstockBatchProfileTargetScorableCandidate {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    requireNonNull(candidate, "profile target distance candidate cannot be null");
  }
}
