package org.craft.mcs.cap.core.appleprofile.application.catalog.port.in;

import org.craft.mcs.cap.core.appleprofile.domain.model.AppleVarietyName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

public record CreateAppleVarietyCommand(
    @NonNull AppleVarietyName name,
    @NonNull ChemicalProfile chemicalProfile
) {

  public CreateAppleVarietyCommand {
    requireNonNull(name, "apple variety name cannot be null");
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
  }
}
