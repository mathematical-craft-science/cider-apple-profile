package org.craft.mcs.cap.shared.domain.chemical.capability;

import java.util.Set;
import org.craft.mcs.cap.shared.domain.classification.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ChemicalProfileCapabilityAssessment(
    @NonNull ChemicalProfileCapability capability,
    boolean capable,
    @NonNull Set<@NonNull ChemicalAxis> requiredAxes,
    @NonNull Set<@NonNull ChemicalAxis> presentAxes,
    @NonNull Set<@NonNull ChemicalAxis> missingAxes
) {

  public ChemicalProfileCapabilityAssessment {
    requireNonNull(capability, "chemical profile capability cannot be null");
    requiredAxes = Set.copyOf(requireNonNull(requiredAxes, "required chemical axes cannot be null"));
    presentAxes = Set.copyOf(requireNonNull(presentAxes, "present chemical axes cannot be null"));
    missingAxes = Set.copyOf(requireNonNull(missingAxes, "missing chemical axes cannot be null"));
  }
}
