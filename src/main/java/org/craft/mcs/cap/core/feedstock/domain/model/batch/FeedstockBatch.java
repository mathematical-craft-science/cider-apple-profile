package org.craft.mcs.cap.core.feedstock.domain.model.batch;

import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.sample.value.CiderMakerId;
import org.craft.mcs.cap.shared.domain.measurement.VolumeValue;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


@AggregateRoot
public final class FeedstockBatch {

  @Identity
  private final @NonNull FeedstockBatchId batchId;

  private final @NonNull CiderMakerId ciderMakerId;

  private @NonNull FeedstockBatchName name;

  private @NonNull FeedstockSeason season;

  private @Nullable FeedstockSource source;

  private @NonNull VolumeValue availableVolume;

  private @Nullable String note;

  private FeedstockBatch(
      @NonNull FeedstockBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockBatchName name,
      @NonNull FeedstockSeason season,
      @Nullable FeedstockSource source,
      @NonNull VolumeValue availableVolume,
      @Nullable String note
  ) {
    this.batchId = requireNonNull(batchId, "feedstock batch id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.name = requireNonNull(name, "feedstock batch name cannot be null");
    this.season = requireNonNull(season, "feedstock season cannot be null");
    this.source = source;
    this.availableVolume = requireNonNull(availableVolume, "available volume cannot be null");
    this.note = normalize(note);
  }

  public static @NonNull FeedstockBatch create(
      @NonNull FeedstockBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockBatchName name,
      @NonNull FeedstockSeason season,
      @NonNull VolumeValue availableVolume
  ) {
    return new FeedstockBatch(
        batchId,
        ciderMakerId,
        name,
        season,
        null,
        availableVolume,
        null
    );
  }

  public @NonNull FeedstockBatchId batchId() {
    return batchId;
  }

  public @NonNull CiderMakerId ciderMakerId() {
    return ciderMakerId;
  }

  public @NonNull FeedstockBatchName name() {
    return name;
  }

  public @NonNull FeedstockSeason season() {
    return season;
  }

  public @Nullable FeedstockSource source() {
    return source;
  }

  public @NonNull VolumeValue availableVolume() {
    return availableVolume;
  }

  public @Nullable String note() {
    return note;
  }

  public @NonNull FeedstockBatch rename(@NonNull FeedstockBatchName name) {
    this.name = requireNonNull(name, "feedstock batch name cannot be null");
    return this;
  }

  public @NonNull FeedstockBatch changeSeason(@NonNull FeedstockSeason season) {
    this.season = requireNonNull(season, "feedstock season cannot be null");
    return this;
  }

  public @NonNull FeedstockBatch changeSource(@Nullable FeedstockSource source) {
    this.source = source;
    return this;
  }

  public @NonNull FeedstockBatch clearSource() {
    this.source = null;
    return this;
  }

  public @NonNull FeedstockBatch changeAvailableVolume(@NonNull VolumeValue availableVolume) {
    this.availableVolume = requireNonNull(availableVolume, "available volume cannot be null");
    return this;
  }

  public @NonNull FeedstockBatch changeNote(@Nullable String note) {
    this.note = normalize(note);
    return this;
  }

  public @NonNull FeedstockBatch clearNote() {
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
