package org.craft.mcs.cap.core.appleprofile.analysis.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** AppleProfile-owned SG/TA graph evaluation facts shared by Apple analysis scenarios. */
public record AppleSugarAcidityGraphView(
    @NonNull Set<@NonNull MissingCharacteristic> missingCharacteristics,
    @Nullable Point point,
    @Nullable ZoneAssessment zoneAssessment
) {

  public AppleSugarAcidityGraphView {
    missingCharacteristics = Set.copyOf(
        requireNonNull(missingCharacteristics, "missing graph characteristics cannot be null")
    );
    final var complete = point != null && zoneAssessment != null;
    final var absent = point == null && zoneAssessment == null;
    if ((missingCharacteristics.isEmpty() && !complete)
        || (!missingCharacteristics.isEmpty() && !absent)) {
      throw new ApplicationValidationException("apple graph evaluation has inconsistent shape");
    }
  }

  public boolean plottable() {
    return point != null;
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

  public enum MissingCharacteristic { SPECIFIC_GRAVITY, TITRATABLE_ACIDITY }

  public record ZoneAssessment(@Nullable Zone zone) {

    public boolean classified() {
      return zone != null;
    }
  }

  public enum Zone {
    PERFECT_APPLES,
    HIGH_SUGAR_APPLES,
    LOW_ACIDITY_APPLES,
    MEDIUM_SUGAR_APPLES,
    LOW_SUGAR_APPLES
  }
}
