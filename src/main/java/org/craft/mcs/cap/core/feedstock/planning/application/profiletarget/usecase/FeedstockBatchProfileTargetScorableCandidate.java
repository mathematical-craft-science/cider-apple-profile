package org.craft.mcs.cap.core.feedstock.planning.application.profiletarget.usecase;

import org.craft.mcs.cap.core.profileanalysis.profiletarget.scoring.application.port.in.ProfileTargetDistanceCandidate;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


record FeedstockBatchProfileTargetScorableCandidate(
    @NonNull FeedstockCurrentProfileView currentProfile,
    @NonNull ProfileTargetDistanceCandidate candidate
) {

  FeedstockBatchProfileTargetScorableCandidate {
    requireNonNull(currentProfile, "feedstock current profile cannot be null");
    requireNonNull(candidate, "profile target distance candidate cannot be null");
  }
}
