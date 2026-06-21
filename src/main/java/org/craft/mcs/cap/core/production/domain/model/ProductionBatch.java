package org.craft.mcs.cap.core.production.domain.model;

import org.craft.mcs.cap.shared.kernel.identity.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionTargetDistance;
import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate root for a confirmed production lot.
 *
 * <p>The aggregate owns production state after a material input has been accepted: product kind,
 * planned status, current volume and the chemical profile snapshot at creation time. Source tree and
 * source snapshots belong to Batch Traceability, not to this aggregate.
 */
@AggregateRoot
public final class ProductionBatch {

  @Identity
  private final @NonNull ProductionBatchId batchId;

  private final @NonNull CiderMakerId ciderMakerId;

  private @NonNull ProductionBatchName name;

  private @NonNull ProductionBatchKind kind;

  private @NonNull ProductionBatchStatus status;

  private @NonNull VolumeValue volume;

  private @NonNull ProductionProfileSnapshot profile;

  private @Nullable ProductionTargetDistance targetDistance;

  private @Nullable String note;

  @SuppressWarnings("java:S107")
  private ProductionBatch(
      @NonNull ProductionBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionBatchName name,
      @NonNull ProductionBatchKind kind,
      @NonNull ProductionBatchStatus status,
      @NonNull VolumeValue volume,
      @NonNull ProductionProfileSnapshot profile,
      @Nullable ProductionTargetDistance targetDistance,
      @Nullable String note
  ) {
    this.batchId = requireNonNull(batchId, "production batch id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.name = requireNonNull(name, "production batch name cannot be null");
    this.kind = requireNonNull(kind, "production batch kind cannot be null");
    this.status = requireNonNull(status, "production batch status cannot be null");
    this.volume = requireNonNull(volume, "production volume cannot be null");
    this.profile = requireNonNull(profile, "production profile cannot be null");
    this.targetDistance = targetDistance;
    this.note = normalize(note);
  }

  @SuppressWarnings("java:S107")
  public static @NonNull ProductionBatch create(
      @NonNull ProductionBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionBatchName name,
      @NonNull ProductionBatchKind kind,
      @NonNull VolumeValue volume,
      @NonNull ProductionProfileSnapshot profile,
      @Nullable ProductionTargetDistance targetDistance,
      @Nullable String note
  ) {
    return new ProductionBatch(
        batchId,
        ciderMakerId,
        name,
        kind,
        ProductionBatchStatus.PLANNED,
        volume,
        profile,
        targetDistance,
        note
    );
  }

  @SuppressWarnings("java:S107")
  public static @NonNull ProductionBatch restore(
      @NonNull ProductionBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionBatchName name,
      @NonNull ProductionBatchKind kind,
      @NonNull ProductionBatchStatus status,
      @NonNull VolumeValue volume,
      @NonNull ProductionProfileSnapshot profile,
      @Nullable ProductionTargetDistance targetDistance,
      @Nullable String note
  ) {
    return new ProductionBatch(
        batchId,
        ciderMakerId,
        name,
        kind,
        status,
        volume,
        profile,
        targetDistance,
        note
    );
  }

  public @NonNull ProductionBatchId batchId() {
    return batchId;
  }

  public @NonNull CiderMakerId ciderMakerId() {
    return ciderMakerId;
  }

  public @NonNull ProductionBatchName name() {
    return name;
  }

  public @NonNull ProductionBatchKind kind() {
    return kind;
  }

  public @NonNull ProductionBatchStatus status() {
    return status;
  }

  public @NonNull VolumeValue volume() {
    return volume;
  }

  public @NonNull ProductionProfileSnapshot profile() {
    return profile;
  }

  public @Nullable ProductionTargetDistance targetDistance() {
    return targetDistance;
  }

  public @Nullable String note() {
    return note;
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
