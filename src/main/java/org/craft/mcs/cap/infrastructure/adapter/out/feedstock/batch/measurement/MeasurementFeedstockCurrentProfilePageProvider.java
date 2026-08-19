package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.batch.measurement;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePagePosition;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfilePageQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfilePageSubjectQuery;
import org.craft.mcs.cap.core.feedstock.batch.application.currentprofile.port.out.FeedstockCurrentProfilePageProvider;
import org.craft.mcs.cap.core.feedstock.batch.application.api.currentprofile.FeedstockCurrentProfileView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Applies keyset pagination to Measurement-backed Feedstock current profiles. */
@Component
@RequiredArgsConstructor
final class MeasurementFeedstockCurrentProfilePageProvider
    implements FeedstockCurrentProfilePageProvider {

  private static final Comparator<FeedstockCurrentProfileView> CANDIDATE_ORDER =
      Comparator.comparing(
              (FeedstockCurrentProfileView view) -> view.latestMeasurementSet().measuredAt(),
              Comparator.reverseOrder()
          )
          .thenComparing(
              view -> view.latestMeasurementSet().measurementSetId().toString(),
              Comparator.reverseOrder()
          );

  private final @NonNull FeedstockCurrentProfilePageSubjectQuery subjectQuery;
  private final @NonNull MeasurementFeedstockCurrentProfileProjector projector;

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findProfiles(
      @NonNull FeedstockCurrentProfilePageQuery query
  ) {
    requireNonNull(query, "feedstock current profile page query cannot be null");
    return projector.project(subjectQuery.findSubjects(query)).stream()
        .sorted(CANDIDATE_ORDER)
        .filter(view -> isAfter(view, query.after()))
        .limit(query.limit())
        .toList();
  }

  private static boolean isAfter(
      @NonNull FeedstockCurrentProfileView view,
      @Nullable FeedstockCurrentProfilePagePosition position
  ) {
    if (position == null) {
      return true;
    }
    final var latestMeasurementSet = view.latestMeasurementSet();
    final var timeComparison = latestMeasurementSet.measuredAt().compareTo(position.measuredAt());
    return timeComparison < 0
        || timeComparison == 0
        && latestMeasurementSet.measurementSetId().toString().compareTo(position.measurementSetId().toString()) < 0;
  }
}
