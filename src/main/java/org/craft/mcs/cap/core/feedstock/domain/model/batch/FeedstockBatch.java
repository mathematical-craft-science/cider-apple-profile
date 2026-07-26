package org.craft.mcs.cap.core.feedstock.domain.model.batch;

import java.time.Instant;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSeason;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockSource;
import org.craft.mcs.cap.core.feedstock.domain.model.batch.value.FeedstockType;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.FeedstockBatchId;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
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

  private @NonNull BatchLifecycle lifecycle;

  private @NonNull VolumeValue availableVolume;

  private @NonNull MaterialStateId currentMaterialStateId;

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
      @NonNull BatchLifecycle lifecycle,
      @NonNull VolumeValue availableVolume,
      @NonNull MaterialStateId currentMaterialStateId,
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
    this.lifecycle = requireNonNull(lifecycle, "feedstock batch lifecycle cannot be null");
    this.availableVolume = requireNonNull(availableVolume, "available volume cannot be null");
    this.currentMaterialStateId = requireNonNull(
        currentMaterialStateId,
        "feedstock current material state id cannot be null"
    );
    this.note = normalize(note);
    this.deletedAt = deletedAt;
    validateMaterialState(lifecycle, availableVolume);
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
        BatchLifecycle.ACTIVE,
        availableVolume,
        MaterialStateId.newId(),
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
      @NonNull BatchLifecycle lifecycle,
      @NonNull VolumeValue availableVolume,
      @NonNull MaterialStateId currentMaterialStateId,
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
        lifecycle,
        availableVolume,
        currentMaterialStateId,
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

  public @NonNull BatchLifecycle lifecycle() {
    return lifecycle;
  }

  public @NonNull VolumeValue availableVolume() {
    return availableVolume;
  }

  public @NonNull MaterialStateId currentMaterialStateId() {
    return currentMaterialStateId;
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

  public @NonNull FeedstockBatch consumeAvailableVolume(@NonNull VolumeValue volume) {
    requireActive();
    requireNonNull(volume, "consumed feedstock volume cannot be null");
    if (volume.isZero()) {
      throw new DomainRuleViolationException("consumed feedstock volume must be positive");
    }
    if (volume.exceeds(availableVolume)) {
      throw new DomainConflictException("consumed feedstock volume exceeds available volume");
    }

    this.availableVolume = availableVolume.subtract(volume);
    currentMaterialStateId = MaterialStateId.newId();
    if (availableVolume.isZero()) {
      lifecycle = BatchLifecycle.DEPLETED;
    }
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

  private static void requirePositiveAvailableVolume(@NonNull VolumeValue availableVolume) {
    requireNonNull(availableVolume, "available volume cannot be null");
    if (availableVolume.isZero()) {
      throw new DomainRuleViolationException("feedstock batch available volume must be greater than zero");
    }
  }

  private void requireActive() {
    if (deleted()) {
      throw new DomainConflictException("cannot consume deleted feedstock batch");
    }
    if (lifecycle != BatchLifecycle.ACTIVE) {
      throw new DomainConflictException(
          "cannot consume feedstock batch when lifecycle is " + lifecycle
      );
    }
  }

  private static void validateMaterialState(
      @NonNull BatchLifecycle lifecycle,
      @NonNull VolumeValue availableVolume
  ) {
    if (lifecycle == BatchLifecycle.ACTIVE && availableVolume.isZero()) {
      throw new DomainRuleViolationException("active feedstock batch volume must be greater than zero");
    }
    if (lifecycle != BatchLifecycle.ACTIVE && !availableVolume.isZero()) {
      throw new DomainRuleViolationException("inactive feedstock batch volume must be zero");
    }
  }
}
