package org.craft.mcs.cap.core.feedstock.domain.model;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockSampleName;
import org.craft.mcs.cap.core.feedstock.domain.model.value.FeedstockType;
import org.craft.mcs.cap.shared.domain.chemical.ChemicalProfile;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@AggregateRoot
public final class FeedstockSample {

  @Identity
  private final @NonNull FeedstockSampleId sampleId;

  private final @NonNull CiderMakerId ciderMakerId;

  private @NonNull FeedstockSampleName name;

  private @NonNull FeedstockType type;

  private @NonNull ChemicalProfile chemicalProfile;

  private @NonNull Instant collectedAt;

  private @Nullable String note;

  private FeedstockSample(
      @NonNull FeedstockSampleId sampleId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockSampleName name,
      @NonNull FeedstockType type,
      @NonNull ChemicalProfile chemicalProfile,
      @NonNull Instant collectedAt,
      @Nullable String note
  ) {
    this.sampleId = requireNonNull(sampleId, "feedstock sample id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.name = requireNonNull(name, "feedstock sample name cannot be null");
    this.type = requireNonNull(type, "feedstock type cannot be null");
    this.chemicalProfile = requireNonNull(chemicalProfile, "chemical profile cannot be null");
    this.collectedAt = requireNonNull(collectedAt, "collected at cannot be null");
    this.note = normalize(note);
  }

  public static @NonNull FeedstockSample create(
      @NonNull FeedstockSampleId sampleId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockSampleName name,
      @NonNull FeedstockType type,
      @NonNull ChemicalProfile chemicalProfile,
      @NonNull Instant collectedAt
  ) {
    return new FeedstockSample(
        sampleId,
        ciderMakerId,
        name,
        type,
        chemicalProfile,
        collectedAt,
        null
    );
  }

  public @NonNull FeedstockSampleId sampleId() {
    return sampleId;
  }

  public @NonNull CiderMakerId ciderMakerId() {
    return ciderMakerId;
  }

  public @NonNull FeedstockSampleName name() {
    return name;
  }

  public @NonNull FeedstockType type() {
    return type;
  }

  public @NonNull ChemicalProfile chemicalProfile() {
    return chemicalProfile;
  }

  public @NonNull Instant collectedAt() {
    return collectedAt;
  }

  public @Nullable String note() {
    return note;
  }

  public @NonNull FeedstockSample rename(@NonNull FeedstockSampleName name) {
    this.name = requireNonNull(name, "feedstock sample name cannot be null");
    return this;
  }

  public @NonNull FeedstockSample changeType(@NonNull FeedstockType type) {
    this.type = requireNonNull(type, "feedstock type cannot be null");
    return this;
  }

  public @NonNull FeedstockSample changeChemicalProfile(
      @NonNull ChemicalProfile chemicalProfile
  ) {
    this.chemicalProfile = requireNonNull(chemicalProfile, "chemical profile cannot be null");
    return this;
  }

  public @NonNull FeedstockSample changeCollectedAt(@NonNull Instant collectedAt) {
    this.collectedAt = requireNonNull(collectedAt, "collected at cannot be null");
    return this;
  }

  public @NonNull FeedstockSample changeNote(@Nullable String note) {
    this.note = normalize(note);
    return this;
  }

  public @NonNull FeedstockSample clearNote() {
    this.note = null;
    return this;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}