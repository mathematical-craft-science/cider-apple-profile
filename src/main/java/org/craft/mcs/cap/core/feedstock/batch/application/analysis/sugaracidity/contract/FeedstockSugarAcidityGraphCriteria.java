package org.craft.mcs.cap.core.feedstock.batch.application.analysis.sugaracidity.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Feedstock graph filter over consumer-owned graph facts. */
public record FeedstockSugarAcidityGraphCriteria(
    @NonNull Set<FeedstockSugarAcidityGraphView.@NonNull Zone> zones,
    @Nullable Boolean plottable
) {

  public FeedstockSugarAcidityGraphCriteria {
    zones = Set.copyOf(requireNonNull(zones, "feedstock graph zones cannot be null"));
    if (zones.isEmpty() && plottable == null) {
      throw new ApplicationValidationException("feedstock graph criteria cannot be empty");
    }
  }

  public boolean matches(@NonNull FeedstockSugarAcidityGraphView evaluation) {
    requireNonNull(evaluation, "feedstock graph evaluation cannot be null");
    return (plottable == null || plottable == evaluation.plottable()) && matchesZone(evaluation);
  }

  private boolean matchesZone(@NonNull FeedstockSugarAcidityGraphView evaluation) {
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
