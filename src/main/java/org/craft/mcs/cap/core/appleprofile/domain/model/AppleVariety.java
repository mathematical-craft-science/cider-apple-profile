package org.craft.mcs.cap.core.appleprofile.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfileSource;
import org.craft.mcs.cap.shared.kernel.chemical.ConfidenceLevel;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
  private @Nullable ChemicalProfileSource chemicalProfileSource;
  private @Nullable ConfidenceLevel chemicalProfileConfidence;

  private AppleVariety(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull ChemicalProfile chemicalProfile,
      @Nullable ChemicalProfileSource chemicalProfileSource,
      @Nullable ConfidenceLevel chemicalProfileConfidence
  ) {
    this.varietyId = requireNonNull(varietyId, "apple variety id cannot be null");
    this.name = requireNonNull(name, "apple variety name cannot be null");
    this.chemicalProfile = validateCatalogChemicalProfile(chemicalProfile);
    this.chemicalProfileSource = chemicalProfileSource;
    this.chemicalProfileConfidence = chemicalProfileConfidence;
  }

  public static @NonNull AppleVariety create(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull ChemicalProfile chemicalProfile,
      @Nullable ChemicalProfileSource chemicalProfileSource,
      @Nullable ConfidenceLevel chemicalProfileConfidence
  ) {
    return new AppleVariety(
        varietyId,
        name,
        chemicalProfile,
        chemicalProfileSource,
        chemicalProfileConfidence
    );
  }

  public static @NonNull AppleVariety create(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull ChemicalProfile chemicalProfile
  ) {
    return create(varietyId, name, chemicalProfile, null, null);
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

  public @Nullable ChemicalProfileSource chemicalProfileSource() {
    return chemicalProfileSource;
  }

  public @Nullable ConfidenceLevel chemicalProfileConfidence() {
    return chemicalProfileConfidence;
  }

  public @NonNull AppleVariety rename(@NonNull AppleVarietyName name) {
    this.name = requireNonNull(name, "apple variety name cannot be null");
    return this;
  }

  public @NonNull AppleVariety changeChemicalProfile(@NonNull ChemicalProfile chemicalProfile) {
    this.chemicalProfile = validateCatalogChemicalProfile(chemicalProfile);
    return this;
  }

  public @NonNull AppleVariety changeChemicalProfileProvenance(
      @Nullable ChemicalProfileSource source,
      @Nullable ConfidenceLevel confidence
  ) {
    chemicalProfileSource = source;
    chemicalProfileConfidence = confidence;
    return this;
  }

  private static @NonNull ChemicalProfile validateCatalogChemicalProfile(
      @NonNull ChemicalProfile chemicalProfile
  ) {
    requireNonNull(chemicalProfile, "chemical profile cannot be null");
    if (chemicalProfile.yeastAssimilableNitrogen().isPresent()) {
      throw new DomainRuleViolationException(
          "apple variety catalog chemical profile cannot contain yeast-assimilable nitrogen"
      );
    }
    return chemicalProfile;
  }
}
