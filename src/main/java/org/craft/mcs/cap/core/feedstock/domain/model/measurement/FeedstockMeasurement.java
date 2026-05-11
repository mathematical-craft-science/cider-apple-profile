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
    this.measuredAt = requireNonNull(measuredAt, "measured at cannot be null");
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
    this.measuredAt = requireNonNull(measuredAt, "measured at cannot be null");
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
}
