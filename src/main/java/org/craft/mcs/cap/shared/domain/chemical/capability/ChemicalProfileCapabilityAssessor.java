package org.craft.mcs.cap.shared.domain.chemical.capability;

import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalProfileAxes;
import org.jmolecules.ddd.annotation.Service;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@Service
public final class ChemicalProfileCapabilityAssessor {

  public @NonNull ChemicalProfileCapabilityAssessment assess(
      @NonNull ChemicalProfileAxes axes,
      @NonNull ChemicalProfileCapability capability
  ) {
    requireNonNull(axes, "chemical profile axes cannot be null");
    requireNonNull(capability, "chemical profile capability cannot be null");

    final var requiredAxes = requiredAxes(capability);
    final var presentAxes = presentAxes(axes, requiredAxes);
    final var missingAxes = missingAxes(requiredAxes, presentAxes);

    return new ChemicalProfileCapabilityAssessment(
        capability,
        missingAxes.isEmpty(),
        requiredAxes,
        presentAxes,
        missingAxes
    );
  }

  private static @NonNull Set<@NonNull ChemicalAxis> requiredAxes(
      @NonNull ChemicalProfileCapability capability
  ) {
    return switch (capability) {
      case ABV_PREDICTION -> Set.of(ChemicalAxis.SUGAR);
      case SUGAR_ACIDITY_PLOT -> Set.of(ChemicalAxis.SUGAR, ChemicalAxis.ACIDITY);
      case NORTH_AMERICAN_PROFILE, SINGLE_VARIETY_CIDER_ASSESSMENT -> Set.of(
          ChemicalAxis.SUGAR,
          ChemicalAxis.ACIDITY,
          ChemicalAxis.TANNINITY
      );
    };
  }

  private static @NonNull Set<@NonNull ChemicalAxis> presentAxes(
      @NonNull ChemicalProfileAxes axes,
      @NonNull Set<@NonNull ChemicalAxis> requiredAxes
  ) {
    final var presentAxes = EnumSet.noneOf(ChemicalAxis.class);

    for (final var axis : requiredAxes) {
      if (axes.has(axis)) {
        presentAxes.add(axis);
      }
    }

    return presentAxes;
  }

  private static @NonNull Set<@NonNull ChemicalAxis> missingAxes(
      @NonNull Set<@NonNull ChemicalAxis> requiredAxes,
      @NonNull Set<@NonNull ChemicalAxis> presentAxes
  ) {
    final var missingAxes = EnumSet.copyOf(requiredAxes);
    missingAxes.removeAll(presentAxes);
    return missingAxes;
  }
}
