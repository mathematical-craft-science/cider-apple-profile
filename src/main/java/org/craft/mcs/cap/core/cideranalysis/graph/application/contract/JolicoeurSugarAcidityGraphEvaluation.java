package org.craft.mcs.cap.core.cideranalysis.graph.application.contract;

import java.util.Optional;
import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Application result of projecting typed SG/TA characteristics and assessing their graph zone.
 *
 * <p>Tannin interpretation is deliberately outside this graph projection.</p>
 */
public record JolicoeurSugarAcidityGraphEvaluation(
    @NonNull Set<@NonNull MissingCharacteristic> missingCharacteristics,
    @Nullable Point point,
    @Nullable ZoneAssessment zoneAssessment
) {

  public JolicoeurSugarAcidityGraphEvaluation {
    missingCharacteristics = Set.copyOf(
        requireNonNull(missingCharacteristics, "missing graph characteristics cannot be null")
    );
    validateShape(missingCharacteristics, point, zoneAssessment);
  }

  public static @NonNull JolicoeurSugarAcidityGraphEvaluation unplottable(
      @NonNull Set<@NonNull MissingCharacteristic> missingCharacteristics
  ) {
    if (requireNonNull(missingCharacteristics, "missing graph characteristics cannot be null").isEmpty()) {
      throw new ApplicationValidationException("unplottable graph evaluation requires missing characteristics");
    }
    return new JolicoeurSugarAcidityGraphEvaluation(
        missingCharacteristics,
        null,
        null
    );
  }

  public static @NonNull JolicoeurSugarAcidityGraphEvaluation plottable(
      @NonNull Point point,
      @NonNull ZoneAssessment zoneAssessment
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

  public @NonNull Optional<@NonNull Point> plottablePoint() {
    return Optional.ofNullable(point);
  }

  private static void validateShape(
      @NonNull Set<@NonNull MissingCharacteristic> missingCharacteristics,
      @Nullable Point point,
      @Nullable ZoneAssessment zoneAssessment
  ) {
    final var hasCompleteResult = point != null && zoneAssessment != null;
    final var hasNoResult = point == null && zoneAssessment == null;
    if (missingCharacteristics.isEmpty() && !hasCompleteResult) {
      throw new ApplicationValidationException("plottable graph evaluation requires point and zone assessment");
    }
    if (!missingCharacteristics.isEmpty() && !hasNoResult) {
      throw new ApplicationValidationException("unplottable graph evaluation cannot contain graph results");
    }
  }

  public record Point(
      @NonNull SpecificGravityValue specificGravity,
      @NonNull TitratableAcidityValue titratableAcidity
  ) {

    public Point {
      requireNonNull(specificGravity, "graph specific gravity cannot be null");
      requireNonNull(titratableAcidity, "graph titratable acidity cannot be null");
    }
  }

  public record ZoneAssessment(@Nullable JolicoeurSugarAcidityZoneCode zone) {

    public boolean classified() {
      return zone != null;
    }
  }

  public enum MissingCharacteristic {
    SPECIFIC_GRAVITY,
    TITRATABLE_ACIDITY
  }

}
