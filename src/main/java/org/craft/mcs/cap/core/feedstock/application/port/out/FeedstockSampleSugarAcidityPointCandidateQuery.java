package org.craft.mcs.cap.core.feedstock.application.port.out;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.application.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockSampleSugarAcidityPointCandidateQuery(
    @NonNull CiderMakerId ciderMakerId,
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockSampleSugarAcidityPointCandidateCursor after,
    int limit
) {

  public FeedstockSampleSugarAcidityPointCandidateQuery {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    if (limit < 1 || limit > CursorPageRequest.MAX_LIMIT + 1) {
      throw new IllegalArgumentException("candidate query limit must be between 1 and " + (CursorPageRequest.MAX_LIMIT + 1));
    }
  }
}
