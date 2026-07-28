package org.craft.mcs.cap.core.profileanalysis.profileaxis.domain;

import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalProfileAxes;
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

    return new ChemicalProfileCapabilityAssessment(
        capability,
        requiredAxes,
        presentAxes
    );
  }

  private static @NonNull Set<@NonNull ChemicalAxis> requiredAxes(
      @NonNull ChemicalProfileCapability capability
  ) {
    return switch (capability) {
      case ABV_PREDICTION -> Set.of(ChemicalAxis.SPECIFIC_GRAVITY);
      case SUGAR_ACIDITY_GRAPH -> Set.of(ChemicalAxis.SPECIFIC_GRAVITY, ChemicalAxis.TITRATABLE_ACIDITY);
      case NORTH_AMERICAN_PROFILE -> Set.of(
          ChemicalAxis.SPECIFIC_GRAVITY,
          ChemicalAxis.TITRATABLE_ACIDITY,
          ChemicalAxis.TANNIN
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
}
