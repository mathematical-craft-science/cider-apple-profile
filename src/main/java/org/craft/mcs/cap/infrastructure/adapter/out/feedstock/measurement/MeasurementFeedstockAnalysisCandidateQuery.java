package org.craft.mcs.cap.infrastructure.adapter.out.feedstock.measurement;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.model.FeedstockAnalysisCandidatePagePosition;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.CurrentFeedstockBatchAnalysisCandidateQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockAnalysisCandidateSubjectQuery;
import org.craft.mcs.cap.core.feedstock.application.analysis.common.port.out.FeedstockCurrentProfileAnalysisCandidateRepository;
import org.craft.mcs.cap.core.feedstock.application.currentprofile.model.FeedstockCurrentProfileView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;


/** Applies analysis pagination to Measurement-backed Feedstock current-profile candidates. */
@Component
@RequiredArgsConstructor
final class MeasurementFeedstockAnalysisCandidateQuery
    implements FeedstockCurrentProfileAnalysisCandidateRepository {

  private static final Comparator<FeedstockCurrentProfileView> CANDIDATE_ORDER =
      Comparator.comparing(
              (FeedstockCurrentProfileView view) -> view.latestMeasurementSet().measuredAt(),
              Comparator.reverseOrder()
          )
          .thenComparing(
              view -> view.latestMeasurementSet().measurementSetId().toString(),
              Comparator.reverseOrder()
          );

  private final @NonNull FeedstockAnalysisCandidateSubjectQuery subjectQuery;
  private final @NonNull MeasurementFeedstockCurrentProfileProjector projector;

  @Override
  public @NonNull List<@NonNull FeedstockCurrentProfileView> findCandidates(
      @NonNull CurrentFeedstockBatchAnalysisCandidateQuery query
  ) {
    requireNonNull(query, "current feedstock batch analysis candidate query cannot be null");
    return projector.project(subjectQuery.findCandidates(query)).stream()
        .sorted(CANDIDATE_ORDER)
        .filter(view -> isAfter(view, query.after()))
        .limit(query.limit())
        .toList();
  }

  private static boolean isAfter(
      @NonNull FeedstockCurrentProfileView view,
      @Nullable FeedstockAnalysisCandidatePagePosition position
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
