package org.craft.mcs.cap.core.feedstock.planning.application.graph.contract;

import java.util.Set;
import org.craft.mcs.cap.shared.application.ApplicationValidationException;
import org.craft.mcs.cap.shared.kernel.chemical.value.SpecificGravityValue;
import org.craft.mcs.cap.shared.kernel.chemical.value.TitratableAcidityValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/** Planning-owned SG/TA graph facts used only for source filtering and target candidate selection. */
public record FeedstockPlanningGraphView(
    @NonNull Set<@NonNull MissingCharacteristic> missingCharacteristics,
    @Nullable Point point,
    @Nullable ZoneAssessment zoneAssessment
) {

  public FeedstockPlanningGraphView {
    missingCharacteristics = Set.copyOf(
        requireNonNull(missingCharacteristics, "missing planning graph characteristics cannot be null")
    );
    final var complete = point != null && zoneAssessment != null;
    final var absent = point == null && zoneAssessment == null;
    if ((missingCharacteristics.isEmpty() && !complete)
        || (!missingCharacteristics.isEmpty() && !absent)) {
      throw new ApplicationValidationException("feedstock planning graph evaluation has inconsistent shape");
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
      requireNonNull(specificGravity, "planning graph specific gravity cannot be null");
      requireNonNull(titratableAcidity, "planning graph titratable acidity cannot be null");
    }
  }

  public enum MissingCharacteristic { SPECIFIC_GRAVITY, TITRATABLE_ACIDITY }

  public record ZoneAssessment(@Nullable Zone zone) {
  }

  public enum Zone {
    PERFECT_APPLES,
    HIGH_SUGAR_APPLES,
    LOW_ACIDITY_APPLES,
    MEDIUM_SUGAR_APPLES,
    LOW_SUGAR_APPLES
  }
}
