package org.craft.mcs.cap.shared.platform.query;

import org.craft.mcs.cap.shared.kernel.chemical.capability.ChemicalProfileCapability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


public record ChemicalProfileCapabilityCriteria(
    @NonNull ChemicalProfileCapability capability,
    @Nullable Boolean capable
) {

  public ChemicalProfileCapabilityCriteria {
    requireNonNull(capability, "chemical profile capability cannot be null");
  }

  public static @NonNull ChemicalProfileCapabilityCriteria capable(
      @NonNull ChemicalProfileCapability capability
  ) {
    return new ChemicalProfileCapabilityCriteria(capability, true);
  }

  public static @NonNull ChemicalProfileCapabilityCriteria notCapable(
      @NonNull ChemicalProfileCapability capability
  ) {
    return new ChemicalProfileCapabilityCriteria(capability, false);
  }
}
