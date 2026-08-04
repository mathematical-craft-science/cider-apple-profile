package org.craft.mcs.cap.core.appleprofile.domain.model;

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
 * observation. Its reference profile contains the catalog-level characteristics currently known for the variety
 * and is used by classification, graph plotting, potential ABV and future catalog discovery use
 * cases. Region-specific contributed observations should live in a separate regional-profile
 * aggregate rather than changing this semantic boundary.
 */
@AggregateRoot
public final class AppleVariety {

  @Identity
  private final @NonNull AppleVarietyId varietyId;

  private @NonNull AppleVarietyName name;

  private @NonNull AppleVarietyReferenceProfile referenceProfile;
  private @Nullable ReferenceProfileSource referenceProfileSource;
  private @Nullable ConfidenceLevel referenceProfileConfidence;

  private AppleVariety(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull AppleVarietyReferenceProfile referenceProfile,
      @Nullable ReferenceProfileSource referenceProfileSource,
      @Nullable ConfidenceLevel referenceProfileConfidence
  ) {
    this.varietyId = requireNonNull(varietyId, "apple variety id cannot be null");
    this.name = requireNonNull(name, "apple variety name cannot be null");
    this.referenceProfile = requireNonNull(referenceProfile, "apple variety reference profile cannot be null");
    this.referenceProfileSource = referenceProfileSource;
    this.referenceProfileConfidence = referenceProfileConfidence;
  }

  public static @NonNull AppleVariety create(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull AppleVarietyReferenceProfile referenceProfile,
      @Nullable ReferenceProfileSource referenceProfileSource,
      @Nullable ConfidenceLevel referenceProfileConfidence
  ) {
    return new AppleVariety(
        varietyId,
        name,
        referenceProfile,
        referenceProfileSource,
        referenceProfileConfidence
    );
  }

  public static @NonNull AppleVariety create(
      @NonNull AppleVarietyId varietyId,
      @NonNull AppleVarietyName name,
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    return create(varietyId, name, referenceProfile, null, null);
  }

  public @NonNull AppleVarietyId varietyId() {
    return varietyId;
  }

  public @NonNull AppleVarietyName name() {
    return name;
  }

  public @NonNull AppleVarietyReferenceProfile referenceProfile() {
    return referenceProfile;
  }

  public @Nullable ReferenceProfileSource referenceProfileSource() {
    return referenceProfileSource;
  }

  public @Nullable ConfidenceLevel referenceProfileConfidence() {
    return referenceProfileConfidence;
  }

  public @NonNull AppleVariety rename(@NonNull AppleVarietyName name) {
    this.name = requireNonNull(name, "apple variety name cannot be null");
    return this;
  }

  public @NonNull AppleVariety changeReferenceProfile(
      @NonNull AppleVarietyReferenceProfile referenceProfile
  ) {
    this.referenceProfile = requireNonNull(
        referenceProfile,
        "apple variety reference profile cannot be null"
    );
    return this;
  }

  public @NonNull AppleVariety changeReferenceProfileProvenance(
      @Nullable ReferenceProfileSource source,
      @Nullable ConfidenceLevel confidence
  ) {
    referenceProfileSource = source;
    referenceProfileConfidence = confidence;
    return this;
  }

}
