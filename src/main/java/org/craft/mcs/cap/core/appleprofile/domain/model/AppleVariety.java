package org.craft.mcs.cap.core.appleprofile.domain.model;

import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

@AggregateRoot
public final class AppleVariety {

  @Identity
  private final @NonNull AppleVarietyId varietyId;

  private @NonNull AppleVarietyName name;

  private @NonNull ChemicalProfile chemicalProfile;

  private AppleVariety(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull ChemicalProfile chemicalProfile
  ) {
    this.varietyId = requireNonNull(varietyId, "apple variety id cannot be null");
    this.name = requireNonNull(name, "apple variety name cannot be null");
    this.chemicalProfile = requireNonNull(chemicalProfile, "chemical profile cannot be null");
  }

  public static @NonNull AppleVariety create(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull ChemicalProfile chemicalProfile
  ) {
    return new AppleVariety(varietyId, name, chemicalProfile);
  }

  public @NonNull AppleVarietyId varietyId() {
    return varietyId;
  }

  public @NonNull AppleVarietyName name() {
    return name;
  }

  public @NonNull ChemicalProfile chemicalProfile() {
    return chemicalProfile;
  }

  public @NonNull AppleVariety rename(@NonNull AppleVarietyName name) {
    this.name = requireNonNull(name, "apple variety name cannot be null");
    return this;
  }

  public @NonNull AppleVariety changeChemicalProfile(@NonNull ChemicalProfile chemicalProfile) {
    this.chemicalProfile = requireNonNull(chemicalProfile, "chemical profile cannot be null");
    return this;
  }
}