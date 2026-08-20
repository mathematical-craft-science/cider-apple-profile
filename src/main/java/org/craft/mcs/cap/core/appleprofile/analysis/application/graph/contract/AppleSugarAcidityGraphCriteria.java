package org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** AppleProfile graph filter shared by Apple analysis scenarios. */
public record AppleSugarAcidityGraphCriteria(
    @NonNull Set<AppleSugarAcidityGraphView.@NonNull Zone> zones,
    @Nullable Boolean plottable
) {

  public AppleSugarAcidityGraphCriteria {
    zones = Set.copyOf(requireNonNull(zones, "apple graph zones cannot be null"));
    if (zones.isEmpty() && plottable == null) {
      throw new ApplicationValidationException("apple graph criteria cannot be empty");
    }
  }

  public boolean matches(@NonNull AppleSugarAcidityGraphView evaluation) {
    requireNonNull(evaluation, "apple graph evaluation cannot be null");
    return (plottable == null || plottable == evaluation.plottable()) && matchesZone(evaluation);
  }

  private boolean matchesZone(@NonNull AppleSugarAcidityGraphView evaluation) {
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
