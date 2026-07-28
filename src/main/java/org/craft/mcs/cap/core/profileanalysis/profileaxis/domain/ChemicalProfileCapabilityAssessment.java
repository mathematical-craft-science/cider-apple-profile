package org.craft.mcs.cap.core.profileanalysis.profileaxis.domain;

import java.util.EnumSet;
import java.util.Set;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.axis.ChemicalAxis;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


public record ChemicalProfileCapabilityAssessment(
    @NonNull ChemicalProfileCapability capability,
    @NonNull Set<@NonNull ChemicalAxis> requiredAxes,
    @NonNull Set<@NonNull ChemicalAxis> presentAxes
) {

  public ChemicalProfileCapabilityAssessment {
    requireNonNull(capability, "chemical profile capability cannot be null");
    requiredAxes = Set.copyOf(requireNonNull(requiredAxes, "required chemical axes cannot be null"));
    presentAxes = Set.copyOf(requireNonNull(presentAxes, "present chemical axes cannot be null"));
    if (requiredAxes.isEmpty()) {
      throw new DomainRuleViolationException("chemical profile capability must require at least one axis");
    }
    if (!requiredAxes.containsAll(presentAxes)) {
      throw new DomainRuleViolationException("present chemical axes must be required by the capability");
    }
  }

  public boolean capable() {
    return presentAxes.containsAll(requiredAxes);
  }

  public @NonNull Set<@NonNull ChemicalAxis> missingAxes() {
    final var missingAxes = EnumSet.copyOf(requiredAxes);
    missingAxes.removeAll(presentAxes);
    return Set.copyOf(missingAxes);
  }
}
