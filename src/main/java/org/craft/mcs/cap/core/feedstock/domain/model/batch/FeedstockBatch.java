package org.craft.mcs.cap.core.feedstock.domain.model.batch;

import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import java.time.Instant;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockBatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate root for a physical lot of available raw material owned by a cider maker.
 *
 * <p>A batch is the operational entity a user manages in the workspace: it has a season, optional
 * source/origin, available volume and lifecycle state. Chemical values are intentionally not stored
 * on the batch itself; they are measured over time by {@code FeedstockMeasurement}. Application
 * query services define the current profile as the latest measurement for the batch.
 *
 * <p>The aggregate owns stock invariants such as consuming no more volume than is available. This
 * keeps production-batch creation and future blending reservations anchored to the feedstock
 * bounded context.
 */
@AggregateRoot
public final class FeedstockBatch {

  @Identity
  private final @NonNull FeedstockBatchId batchId;

  private final @NonNull CiderMakerId ciderMakerId;

  private @NonNull FeedstockBatchName name;

  private @NonNull FeedstockType type;

  private @NonNull FeedstockSeason season;

  private @Nullable FeedstockSource source;

  private @NonNull VolumeValue availableVolume;

  private @Nullable String note;

  private @Nullable Instant deletedAt;

  @SuppressWarnings("java:S107")
  private FeedstockBatch(
      @NonNull FeedstockBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockBatchName name,
      @NonNull FeedstockType type,
      @NonNull FeedstockSeason season,
      @Nullable FeedstockSource source,
      @NonNull VolumeValue availableVolume,
      @Nullable String note,
      @Nullable Instant deletedAt
  ) {
    this.batchId = requireNonNull(batchId, "feedstock batch id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.name = requireNonNull(name, "feedstock batch name cannot be null");
    this.type = requireNonNull(type, "feedstock type cannot be null");
    this.season = requireNonNull(season, "feedstock season cannot be null");
    this.source = source;
    this.availableVolume = requireNonNull(availableVolume, "available volume cannot be null");
    this.note = normalize(note);
    this.deletedAt = deletedAt;
  }

  public static @NonNull FeedstockBatch create(
      @NonNull FeedstockBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockBatchName name,
      @NonNull FeedstockType type,
      @NonNull FeedstockSeason season,
      @NonNull VolumeValue availableVolume
  ) {
    return new FeedstockBatch(
        batchId,
        ciderMakerId,
        name,
        type,
        season,
        null,
        availableVolume,
        null,
        null
    );
  }

  @SuppressWarnings("java:S107")
  public static @NonNull FeedstockBatch restore(
      @NonNull FeedstockBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull FeedstockBatchName name,
      @NonNull FeedstockType type,
      @NonNull FeedstockSeason season,
      @Nullable FeedstockSource source,
      @NonNull VolumeValue availableVolume,
      @Nullable String note,
      @Nullable Instant deletedAt
  ) {
    return new FeedstockBatch(
        batchId,
        ciderMakerId,
        name,
        type,
        season,
        source,
        availableVolume,
        note,
        deletedAt
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

  public @NonNull FeedstockType type() {
    return type;
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

  public @Nullable Instant deletedAt() {
    return deletedAt;
  }

  public boolean deleted() {
    return deletedAt != null;
  }

  public @NonNull FeedstockBatch rename(@NonNull FeedstockBatchName name) {
    this.name = requireNonNull(name, "feedstock batch name cannot be null");
    return this;
  }

  public @NonNull FeedstockBatch changeType(@NonNull FeedstockType type) {
    this.type = requireNonNull(type, "feedstock type cannot be null");
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

  public @NonNull FeedstockBatch consumeAvailableVolume(@NonNull VolumeValue volume) {
    requireNonNull(volume, "consumed feedstock volume cannot be null");
    if (volume.isZero()) {
      throw new DomainRuleViolationException("consumed feedstock volume must be positive");
    }
    if (volume.exceeds(availableVolume)) {
      throw new DomainConflictException("consumed feedstock volume exceeds available volume");
    }

    this.availableVolume = availableVolume.subtract(volume);
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

  public @NonNull FeedstockBatch delete(@NonNull Instant deletedAt) {
    this.deletedAt = requireNonNull(deletedAt, "deleted at cannot be null");
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
