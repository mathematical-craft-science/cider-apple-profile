package org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.authorization.port.out.FeedstockAccessControl;
import org.craft.mcs.cap.core.feedstock.batch.application.api.batch.FeedstockBatchSummary;
import org.craft.mcs.cap.core.feedstock.batch.application.batch.port.out.FeedstockBatchSummaryQueryRepository;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageItem;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.in.QueryFeedstockCurrentProfilePageUseCase;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfilePageProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/** Selects current Feedstock evidence and joins only coherent batch metadata owned by this context. */
@Component
@RequiredArgsConstructor
public final class QueryFeedstockCurrentProfilePageService
    implements QueryFeedstockCurrentProfilePageUseCase {

  private final @NonNull FeedstockCurrentProfilePageProvider currentProfileProvider;
  private final @NonNull FeedstockBatchSummaryQueryRepository batchSummaryRepository;
  private final @NonNull FeedstockAccessControl accessControl;

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfilePageItem> query(
      @NonNull FeedstockCurrentProfilePageQuery query
  ) {
    requireNonNull(query, "feedstock current profile page query cannot be null");
    accessControl.requireReadAccess(query.workspaceId());
    final var batchesById = batchSummaryRepository.findAllByWorkspaceId(query.workspaceId()).stream()
        .collect(Collectors.toUnmodifiableMap(FeedstockBatchSummary::batchId, Function.identity()));

    final var items = new ArrayList<FeedstockCurrentProfilePageItem>(query.limit());
    var position = query.after();
    var exhausted = false;
    while (items.size() < query.limit() && !exhausted) {
      final var profiles = currentProfileProvider.findProfiles(query.at(position, query.limit()));
      if (profiles.isEmpty()) {
        break;
      }
      for (final var profile : profiles) {
        position = position(profile);
        final var batch = batchesById.get(profile.batchId());
        if (hasCoherentBatch(batch, profile)) {
          items.add(new FeedstockCurrentProfilePageItem(requireNonNull(batch), profile));
          if (items.size() == query.limit()) {
            break;
          }
        }
      }
      exhausted = profiles.size() < query.limit();
    }
    return List.copyOf(items);
  }

  private static @NonNull FeedstockCurrentProfilePagePosition position(
      @NonNull FeedstockCurrentProfileView profile
  ) {
    final var latestMeasurement = profile.latestMeasurementSet();
    return new FeedstockCurrentProfilePagePosition(
        latestMeasurement.measuredAt(),
        latestMeasurement.measurementSetId()
    );
  }

  private static boolean hasCoherentBatch(
      @Nullable FeedstockBatchSummary batch,
      @NonNull FeedstockCurrentProfileView profile
  ) {
    return batch != null && batch.currentMaterialStateId().equals(profile.currentMaterialStateId());
  }
}
