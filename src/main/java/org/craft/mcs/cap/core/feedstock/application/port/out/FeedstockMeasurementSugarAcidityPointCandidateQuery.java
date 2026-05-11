package org.craft.mcs.cap.core.feedstock.application.port.out;

import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.platform.query.CursorPageRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record FeedstockMeasurementSugarAcidityPointCandidateQuery(
    @NonNull CiderMakerId ciderMakerId,
    @Nullable FeedstockBatchCriteria batch,
    @Nullable FeedstockSeason season,
    @Nullable FeedstockMeasurementSugarAcidityPointCandidateCursor after,
    int limit
) {

  public FeedstockMeasurementSugarAcidityPointCandidateQuery {
    requireNonNull(ciderMakerId, "cider maker id cannot be null");

    if (limit < 1 || limit > CursorPageRequest.MAX_LIMIT + 1) {
      throw new IllegalArgumentException("candidate query limit must be between 1 and " + (CursorPageRequest.MAX_LIMIT + 1));
    }
  }
}
