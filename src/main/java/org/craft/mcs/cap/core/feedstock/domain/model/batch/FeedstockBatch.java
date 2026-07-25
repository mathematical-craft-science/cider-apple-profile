package org.craft.mcs.cap.core.feedstock.domain.model.batch;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate root for a physical lot of available raw material owned by a workspace.
 *
 * <p>A batch is the operational entity a cider maker manages as source material: it has a season,
 * optional vessel, optional source/origin, available volume and lifecycle state. Measurement facts
 * are owned by the generic Measurement context. Application queries project current values from
 * evidence applicable to the batch's current material-state lineage.
 *
 * <p>The aggregate owns stock invariants such as consuming no more volume than is available. This
 * keeps production-batch creation and future blending reservations anchored to the feedstock
 * bounded context.
 */
@AggregateRoot
public final class FeedstockBatch {

  @Identity
  private final @NonNull FeedstockBatchId batchId;

  private final @NonNull WorkspaceId workspaceId;

  private @NonNull BatchName name;

  private @Nullable Vessel vessel;

  private @NonNull FeedstockType type;

  private @NonNull FeedstockSeason season;

  private @Nullable FeedstockSource source;

  private @NonNull VolumeValue availableVolume;

  private @Nullable String note;

  private @Nullable Instant deletedAt;

  @SuppressWarnings("java:S107")
  private FeedstockBatch(
      @NonNull FeedstockBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @Nullable Vessel vessel,
      @NonNull FeedstockType type,
      @NonNull FeedstockSeason season,
      @Nullable FeedstockSource source,
      @NonNull VolumeValue availableVolume,
      @Nullable String note,
      @Nullable Instant deletedAt
  ) {
    this.batchId = requireNonNull(batchId, "feedstock batch id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.name = requireNonNull(name, "feedstock batch name cannot be null");
    this.vessel = vessel;
    this.type = requireNonNull(type, "feedstock type cannot be null");
    this.season = requireNonNull(season, "feedstock season cannot be null");
    this.source = source;
    this.availableVolume = requireNonNull(availableVolume, "available volume cannot be null");
    this.note = normalize(note);
    this.deletedAt = deletedAt;
  }

  public static @NonNull FeedstockBatch create(
      @NonNull FeedstockBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @Nullable Vessel vessel,
      @NonNull FeedstockType type,
      @NonNull FeedstockSeason season,
      @NonNull VolumeValue availableVolume
  ) {
    requirePositiveAvailableVolume(availableVolume);
    return new FeedstockBatch(
        batchId,
        workspaceId,
        name,
        vessel,
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
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @Nullable Vessel vessel,
      @NonNull FeedstockType type,
      @NonNull FeedstockSeason season,
      @Nullable FeedstockSource source,
      @NonNull VolumeValue availableVolume,
      @Nullable String note,
      @Nullable Instant deletedAt
  ) {
    return new FeedstockBatch(
        batchId,
        workspaceId,
        name,
        vessel,
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

  public @NonNull WorkspaceId workspaceId() {
    return workspaceId;
  }

  public @NonNull BatchName name() {
    return name;
  }

  public @Nullable Vessel vessel() {
    return vessel;
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

  public @NonNull FeedstockBatch rename(@NonNull BatchName name) {
    this.name = requireNonNull(name, "feedstock batch name cannot be null");
    return this;
  }

  public @NonNull FeedstockBatch changeVessel(@NonNull Vessel vessel) {
    this.vessel = requireNonNull(vessel, "feedstock vessel cannot be null");
    return this;
  }

  public @NonNull FeedstockBatch clearVessel() {
    this.vessel = null;
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
    this.availableVolume = requirePositiveAvailableVolume(availableVolume);
    return this;
  }

  public @NonNull FeedstockBatch ensureAvailableVolumeIsPositive() {
    requirePositiveAvailableVolume(availableVolume);
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

  private static @NonNull VolumeValue requirePositiveAvailableVolume(@NonNull VolumeValue availableVolume) {
    requireNonNull(availableVolume, "available volume cannot be null");
    if (availableVolume.isZero()) {
      throw new DomainRuleViolationException("feedstock batch available volume must be greater than zero");
    }
    return availableVolume;
  }
}
