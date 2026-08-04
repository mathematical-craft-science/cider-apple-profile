package org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.mapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.craft.mcs.cap.core.feedstock.application.batch.model.FeedstockBatchCriteria;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.infrastructure.adapter.in.rest.feedstock.common.dto.FeedstockSeasonRequest;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Maps common Feedstock transport filters to typed application criteria. */
public final class FeedstockCriteriaRestMapper {

  private FeedstockCriteriaRestMapper() {
  }

  public static @Nullable FeedstockSeason toSeason(@Nullable FeedstockSeasonRequest request) {
    return request == null ? null : FeedstockSeason.of(request.startsOn(), request.endsOn());
  }

  public static @Nullable FeedstockBatchCriteria toBatchCriteria(@Nullable UUID batchId) {
    return batchId == null ? null : FeedstockBatchCriteria.of(FeedstockBatchId.of(batchId));
  }

  public static @Nullable FeedstockBatchCriteria toBatchCriteria(
      @Nullable List<@NonNull UUID> batchIds
  ) {
    if (batchIds == null || batchIds.isEmpty()) {
      return null;
    }
    return new FeedstockBatchCriteria(
        batchIds.stream()
            .map(FeedstockBatchId::of)
            .collect(Collectors.toSet())
    );
  }
}
