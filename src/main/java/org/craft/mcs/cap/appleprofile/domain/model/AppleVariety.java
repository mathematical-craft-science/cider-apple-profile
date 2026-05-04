package org.craft.mcs.cap.appleprofile.domain.model;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

@AggregateRoot
public final class AppleVariety {

  @Identity
  private final AppleVarietyId varietyId;
  private final AppleVarietyName name;
  private final ChemicalProfile chemicalProfile;

  private AppleVariety(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull ChemicalProfile chemicalProfile
  ) {
    this.varietyId = requireNonNull(varietyId, "apple variety id cannot be null");
    this.name = requireNonNull(name, "apple variety name cannot be null");
    this.chemicalProfile = requireNonNull(chemicalProfile, "chemical profile cannot be null");
  }

  @NonNull
  public static AppleVariety create(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull ChemicalProfile chemicalProfile
  ) {
    return new AppleVariety(varietyId, name, chemicalProfile);
  }

  @NonNull
  public AppleVarietyId varietyId() {
    return varietyId;
  }

  @NonNull
  public AppleVarietyName name() {
    return name;
  }

  @NonNull
  public ChemicalProfile chemicalProfile() {
    return chemicalProfile;
  }

  @NonNull
  public AppleVariety withChemicalProfile(@NonNull ChemicalProfile chemicalProfile) {
    return new AppleVariety(varietyId, name, chemicalProfile);
  }
}
