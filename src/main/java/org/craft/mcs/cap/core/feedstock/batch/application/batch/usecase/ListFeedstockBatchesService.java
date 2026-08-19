package org.craft.mcs.cap.core.feedstock.batch.application.batch.usecase;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchView;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.ListFeedstockBatchesQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.in.ListFeedstockBatchesUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.batch.domain.model.batch.value.FeedstockSeason;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


/**
 * Read-side use case for listing feedstock batches owned by a workspace.
 *
 * <p>The service keeps the list endpoint focused on batch metadata and stock state. Optional season
 * filtering uses date-range overlap because cider-making seasons are practical periods rather than fixed global enum
 * values.
 */
@Component
@RequiredArgsConstructor
public final class ListFeedstockBatchesService
    implements ListFeedstockBatchesUseCase {

  private final @NonNull FeedstockBatchSummaryQueryRepository queryRepository;
  private final @NonNull FeedstockAccessControl feedstockAccessControl;
  private final @NonNull FeedstockBatchViewEnricher batchViewEnricher;

  @Override
  public @NonNull List<@NonNull FeedstockBatchView> list(
      @NonNull ListFeedstockBatchesQuery query
  ) {
    requireNonNull(query, "list feedstock batches query cannot be null");
    feedstockAccessControl.requireReadAccess(query.workspaceId());

    final var batches = queryRepository.findAllByWorkspaceId(query.workspaceId())
        .stream()
        .filter(batch -> query.season() == null || overlaps(batch.season(), query.season()))
        .toList();
    return batchViewEnricher.enrichAll(query.workspaceId(), batches);
  }

  private static boolean overlaps(
      @NonNull FeedstockSeason batchSeason,
      @NonNull FeedstockSeason requestedSeason
  ) {
    return !batchSeason.endsOn().isBefore(requestedSeason.startsOn())
        && !batchSeason.startsOn().isAfter(requestedSeason.endsOn());
  }
}
