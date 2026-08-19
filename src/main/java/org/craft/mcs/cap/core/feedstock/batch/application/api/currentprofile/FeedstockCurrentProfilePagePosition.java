package org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable keyset position for a page of current Feedstock profiles. */
public record FeedstockCurrentProfilePagePosition(
    @NonNull Instant measuredAt,
    @NonNull UUID measurementSetId
) {

  public FeedstockCurrentProfilePagePosition {
    requireNonNull(measuredAt, "feedstock measurement page position measuredAt cannot be null");
    requireNonNull(measurementSetId, "feedstock measurement set page position id cannot be null");
  }
}
