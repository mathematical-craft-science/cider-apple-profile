package org.craft.mcs.cap.core.feedstock.application.measurement.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.FeedstockMeasurementSummary;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.in.ListFeedstockMeasurementsUseCase;
import org.craft.mcs.cap.core.feedstock.application.measurement.port.out.FeedstockMeasurementQueryRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Read-side use case for measurement history of one feedstock batch.
 *
 * <p>The returned list is historical evidence for the batch. It is intentionally separate from
 * current-profile queries so UI screens can show both the latest actionable profile and the full
 * timeline without mixing semantics.
 */
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
