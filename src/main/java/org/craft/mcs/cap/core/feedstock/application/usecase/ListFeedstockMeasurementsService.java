package org.craft.mcs.cap.core.feedstock.application.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.application.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public final class ListFeedstockMeasurementsService
    implements ListFeedstockMeasurementsUseCase {

  private final @NonNull FeedstockMeasurementQueryRepository feedstockMeasurementQueryRepository;

  @Override
  public @NonNull List<@NonNull FeedstockMeasurementSummary> listByBatchId(
      @NonNull FeedstockBatchId batchId
  ) {
    requireNonNull(batchId, "feedstock batch id cannot be null");
    return feedstockMeasurementQueryRepository.findSummariesByBatchId(batchId);
  }
}
