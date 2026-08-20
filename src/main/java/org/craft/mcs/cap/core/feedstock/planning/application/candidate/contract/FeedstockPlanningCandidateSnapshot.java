package org.craft.mcs.cap.core.feedstock.planning.application.candidate.contract;

import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Planning snapshot of current Feedstock evidence and its available source volume. */
public record FeedstockPlanningCandidateSnapshot(
    @NonNull FeedstockCurrentProfileView currentProfile,
    @NonNull VolumeValue availableVolume
) {

  public FeedstockPlanningCandidateSnapshot {
    requireNonNull(currentProfile, "feedstock planning current profile cannot be null");
    requireNonNull(availableVolume, "feedstock planning available volume cannot be null");
  }
}
