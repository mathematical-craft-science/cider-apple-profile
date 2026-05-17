package org.craft.mcs.cap.core.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;

/**
 * Aggregate root for a pomological apple variety in the catalog.
 *
 * <p>The aggregate represents the variety itself, not a concrete harvest lot or a regional
 * observation. Its chemical profile is the catalog-level profile currently known for the variety
 * and is used by classification, graph plotting, potential ABV and future catalog discovery use
 * cases. Region-specific contributed observations should live in a separate regional-profile
 * aggregate rather than changing this semantic boundary.
 */
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
