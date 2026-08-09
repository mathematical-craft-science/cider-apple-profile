package org.craft.mcs.cap.core.profileanalysis.graph.application.model;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Optional criteria over a completed Jolicoeur sugar-acidity graph evaluation.
 *
 * <p>Plottability is a graph-specific fact derived from SG and TA availability. Zone matching is
 * evaluated only for plottable points.</p>
 */
public record JolicoeurSugarAcidityGraphCriteria(
    @Nullable JolicoeurSugarAcidityZoneCriteria zone,
    @Nullable Boolean plottable
) {

  /** Reports whether the graph evaluation satisfies all specified criteria. */
  public boolean matches(@NonNull JolicoeurSugarAcidityGraphEvaluation evaluation) {
    requireNonNull(evaluation, "Jolicoeur sugar-acidity graph evaluation cannot be null");
    return matchesPlottability(evaluation) && matchesZone(evaluation);
  }

  private boolean matchesPlottability(@NonNull JolicoeurSugarAcidityGraphEvaluation evaluation) {
    return plottable == null || plottable == evaluation.plottable();
  }

  private boolean matchesZone(@NonNull JolicoeurSugarAcidityGraphEvaluation evaluation) {
    if (zone == null) {
      return true;
    }
    final var assessment = evaluation.zoneAssessment();
    return assessment != null && zone.matches(assessment);
  }
}
