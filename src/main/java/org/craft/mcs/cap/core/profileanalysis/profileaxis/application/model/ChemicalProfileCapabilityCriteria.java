package org.craft.mcs.cap.core.profileanalysis.profileaxis.application.model;

import org.craft.mcs.cap.core.profileanalysis.profileaxis.domain.ChemicalProfileCapability;
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

  public boolean matches(boolean actualCapability) {
    return capable == null || capable == actualCapability;
  }
}
