package org.craft.mcs.cap.core.feedstock.application.batch.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.application.batch.port.out.FeedstockBatchRepository;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatch;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/**
 * Read-side use case for listing feedstock batches owned by a cider maker.
 *
 * <p>The service keeps the list endpoint focused on batch metadata and stock state. Optional season
 * filtering uses date-range overlap because cider-making seasons are practical periods rather than
 * fixed global enum values.
 */
@Component
@RequiredArgsConstructor
public final class ListFeedstockBatchesService
    implements ListFeedstockBatchesUseCase {

  private final @NonNull FeedstockBatchRepository feedstockBatchRepository;

  @Override
  public @NonNull List<@NonNull FeedstockBatchSummary> list(
      @NonNull ListFeedstockBatchesQuery query
  ) {
    requireNonNull(query, "list feedstock batches query cannot be null");

    return feedstockBatchRepository.findAllByCiderMakerId(query.ciderMakerId())
        .stream()
        .filter(batch -> query.season() == null || overlaps(batch.season(), query.season()))
        .map(ListFeedstockBatchesService::toSummary)
        .toList();
  }

  private static boolean overlaps(
      @NonNull FeedstockSeason batchSeason,
      @NonNull FeedstockSeason requestedSeason
  ) {
    return !batchSeason.endsOn().isBefore(requestedSeason.startsOn())
        && !batchSeason.startsOn().isAfter(requestedSeason.endsOn());
  }

  private static @NonNull FeedstockBatchSummary toSummary(@NonNull FeedstockBatch batch) {
    return new FeedstockBatchSummary(
        batch.batchId(),
        batch.ciderMakerId(),
        batch.name(),
        batch.type(),
        batch.season(),
        batch.source(),
        batch.availableVolume(),
        batch.note()
    );
  }
}
