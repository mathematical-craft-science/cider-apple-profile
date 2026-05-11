package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockSampleSummary;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockSamplesUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockSampleQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class ListFeedstockSamplesService
    implements ListFeedstockSamplesUseCase {

  private final @NonNull FeedstockSampleQueryRepository feedstockSampleQueryRepository;

  @Override
  public @NonNull List<@NonNull FeedstockSampleSummary> listByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return feedstockSampleQueryRepository.findSummariesByBatchId(batchId);
  }
}
