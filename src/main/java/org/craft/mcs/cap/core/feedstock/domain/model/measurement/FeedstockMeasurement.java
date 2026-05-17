package org.craft.mcs.cap.core.feedstock.domain.model.measurement;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.FeedstockBatchId;
import org.craft.mcs.cap.core.feedstock.domain.model.measurement.value.FeedstockMeasurementName;
import org.craft.mcs.cap.shared.kernel.chemical.ChemicalProfile;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Aggregate root for one chemical measurement of a feedstock batch.
 *
 * <p>The measurement is historical evidence, not the batch itself. Multiple measurements may exist
 * for the same {@link FeedstockBatchId}; read-side application services select the latest one when
 * a use case needs the current batch profile. This supports gradual profile completion and audit of
 * how raw material characteristics changed over time.
 *
 * <p>The aggregate validates that measurements cannot be dated in the future and owns edits to the
 * measured chemical profile, name and note.
 */
@AggregateRoot
public final class FeedstockMeasurement {

  @Identity
  private final @NonNull FeedstockMeasurementId measurementId;

  private final @NonNull FeedstockBatchId batchId;

  private @NonNull FeedstockMeasurementName name;

  private @NonNull ChemicalProfile chemicalProfile;

  private @NonNull Instant measuredAt;

  private @Nullable String note;

  private FeedstockMeasurement(
      @NonNull FeedstockMeasurementId measurementId,
      @NonNull FeedstockBatchId batchId,
      @NonNull FeedstockMeasurementName name,
      @NonNull ChemicalProfile chemicalProfile,
      @NonNull Instant measuredAt,
      @Nullable String note
  ) {
    this.measurementId = requireNonNull(measurementId, "feedstock measurement id cannot be null");
    this.batchId = requireNonNull(batchId, "feedstock batch id cannot be null");
    this.name = requireNonNull(name, "feedstock measurement name cannot be null");
    this.chemicalProfile = requireNonNull(chemicalProfile, "chemical profile cannot be null");
    this.measuredAt = validMeasuredAt(measuredAt);
    this.note = normalize(note);
  }

  public static @NonNull FeedstockMeasurement create(
      @NonNull FeedstockMeasurementId measurementId,
      @NonNull FeedstockBatchId batchId,
      @NonNull FeedstockMeasurementName name,
      @NonNull ChemicalProfile chemicalProfile,
      @NonNull Instant measuredAt
  ) {
    return new FeedstockMeasurement(
        measurementId,
        batchId,
        name,
        chemicalProfile,
        measuredAt,
        null
    );
  }

  public @NonNull FeedstockMeasurementId measurementId() {
    return measurementId;
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }

  public @NonNull FeedstockMeasurementName name() {
    return name;
  }

  public @NonNull ChemicalProfile chemicalProfile() {
    return chemicalProfile;
  }

  public @NonNull Instant measuredAt() {
    return measuredAt;
  }

  public @Nullable String note() {
    return note;
  }

  public @NonNull FeedstockMeasurement rename(@NonNull FeedstockMeasurementName name) {
    this.name = requireNonNull(name, "feedstock measurement name cannot be null");
    return this;
  }

  public @NonNull FeedstockMeasurement changeChemicalProfile(
      @NonNull ChemicalProfile chemicalProfile
  ) {
    this.chemicalProfile = requireNonNull(chemicalProfile, "chemical profile cannot be null");
    return this;
  }

  public @NonNull FeedstockMeasurement changeMeasuredAt(@NonNull Instant measuredAt) {
    this.measuredAt = validMeasuredAt(measuredAt);
    return this;
  }

  public @NonNull FeedstockMeasurement changeNote(@Nullable String note) {
    this.note = normalize(note);
    return this;
  }

  public @NonNull FeedstockMeasurement clearNote() {
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

  private static @NonNull Instant validMeasuredAt(@NonNull Instant measuredAt) {
    requireNonNull(measuredAt, "measured at cannot be null");
    if (measuredAt.isAfter(Instant.now())) {
      throw new IllegalArgumentException("feedstock measurement date cannot be in the future");
    }
    return measuredAt;
  }
}
