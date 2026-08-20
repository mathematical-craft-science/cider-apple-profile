package org.craft.mcs.cap.core.feedstock.planning.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Planning-owned source filter over immutable SG/TA graph facts. */
public record FeedstockPlanningGraphCriteria(
    @NonNull Set<FeedstockPlanningGraphView.@NonNull Zone> zones,
    @Nullable Boolean plottable
) {

  public FeedstockPlanningGraphCriteria {
    zones = Set.copyOf(requireNonNull(zones, "feedstock planning graph zones cannot be null"));
    if (zones.isEmpty() && plottable == null) {
      throw new ApplicationValidationException("feedstock planning graph criteria cannot be empty");
    }
  }

  public boolean matches(@NonNull FeedstockPlanningGraphView evaluation) {
    requireNonNull(evaluation, "feedstock planning graph evaluation cannot be null");
    return (plottable == null || plottable == evaluation.plottable()) && matchesZone(evaluation);
  }

  private boolean matchesZone(@NonNull FeedstockPlanningGraphView evaluation) {
    if (zones.isEmpty()) {
      return true;
    }
    final var assessment = evaluation.zoneAssessment();
    if (assessment == null) {
      return false;
    }
    final var assessedZone = assessment.zone();
    return assessedZone != null && zones.contains(assessedZone);
  }
}
