package org.craft.mcs.cap.core.feedstock.application.analysis.common.model;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable keyset position shared by paged feedstock analysis-candidate queries. */
public record FeedstockAnalysisCandidatePagePosition(
    @NonNull Instant measuredAt,
    @NonNull UUID measurementSetId
) {

  public FeedstockAnalysisCandidatePagePosition {
    requireNonNull(measuredAt, "feedstock measurement page position measuredAt cannot be null");
    requireNonNull(measurementSetId, "feedstock measurement set page position id cannot be null");
  }
}
