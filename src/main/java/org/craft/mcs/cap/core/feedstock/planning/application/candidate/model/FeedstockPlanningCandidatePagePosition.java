package org.craft.mcs.cap.core.feedstock.planning.application.candidate.model;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/** Planning-owned cursor position for Feedstock candidate selection. */
public record FeedstockPlanningCandidatePagePosition(
    @NonNull Instant measuredAt,
    @NonNull UUID measurementSetId
) {

  public FeedstockPlanningCandidatePagePosition {
    requireNonNull(measuredAt, "feedstock planning candidate measuredAt cannot be null");
    requireNonNull(measurementSetId, "feedstock planning candidate measurement set id cannot be null");
  }
}
