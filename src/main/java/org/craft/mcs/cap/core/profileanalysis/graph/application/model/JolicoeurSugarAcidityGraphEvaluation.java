package org.craft.mcs.cap.core.profileanalysis.graph.application.model;

import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.GraphMissingCharacteristic;
import org.craft.mcs.cap.core.profileanalysis.graph.domain.SugarAcidityGraphPoint;
import org.craft.mcs.cap.core.profileanalysis.graph.zone.domain.JolicoeurSugarAcidityZoneAssessment;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Application result of projecting typed SG/TA characteristics and assessing their graph zone.
 *
 * <p>Tannin interpretation is deliberately outside this graph projection.</p>
 */
public record JolicoeurSugarAcidityGraphEvaluation(
    @NonNull Set<@NonNull GraphMissingCharacteristic> missingCharacteristics,
    @Nullable SugarAcidityGraphPoint point,
    @Nullable JolicoeurSugarAcidityZoneAssessment zoneAssessment
) {

  public JolicoeurSugarAcidityGraphEvaluation {
    missingCharacteristics = Set.copyOf(
        requireNonNull(missingCharacteristics, "missing graph characteristics cannot be null")
    );
    validateShape(missingCharacteristics, point, zoneAssessment);
  }

  public static @NonNull JolicoeurSugarAcidityGraphEvaluation unplottable(
      @NonNull Set<@NonNull GraphMissingCharacteristic> missingCharacteristics
  ) {
    if (requireNonNull(missingCharacteristics, "missing graph characteristics cannot be null").isEmpty()) {
      throw new DomainRuleViolationException("unplottable graph evaluation requires missing characteristics");
    }
    return new JolicoeurSugarAcidityGraphEvaluation(
        missingCharacteristics,
        null,
        null
    );
  }

  public static @NonNull JolicoeurSugarAcidityGraphEvaluation plottable(
      @NonNull SugarAcidityGraphPoint point,
      @NonNull JolicoeurSugarAcidityZoneAssessment zoneAssessment
  ) {
    return new JolicoeurSugarAcidityGraphEvaluation(
        Set.of(),
        point,
        zoneAssessment
    );
  }

  public boolean plottable() {
    return point != null;
  }

  public @NonNull Optional<@NonNull SugarAcidityGraphPoint> plottablePoint() {
    return Optional.ofNullable(point);
  }

  private static void validateShape(
      @NonNull Set<@NonNull GraphMissingCharacteristic> missingCharacteristics,
      @Nullable SugarAcidityGraphPoint point,
      @Nullable JolicoeurSugarAcidityZoneAssessment zoneAssessment
  ) {
    final var hasCompleteResult = point != null && zoneAssessment != null;
    final var hasNoResult = point == null && zoneAssessment == null;
    if (missingCharacteristics.isEmpty() && !hasCompleteResult) {
      throw new DomainRuleViolationException("plottable graph evaluation requires point and zone assessment");
    }
    if (!missingCharacteristics.isEmpty() && !hasNoResult) {
      throw new DomainRuleViolationException("unplottable graph evaluation cannot contain graph results");
    }
  }
}
