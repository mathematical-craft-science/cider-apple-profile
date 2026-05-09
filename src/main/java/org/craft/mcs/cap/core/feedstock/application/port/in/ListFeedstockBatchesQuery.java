package org.craft.mcs.cap.core.feedstock.application.port.in;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ListFeedstockBatchesQuery(
    @NonNull CiderMakerId ciderMakerId,
    @Nullable FeedstockSeason season
) {

  public ListFeedstockBatchesQuery {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");
  }
}
