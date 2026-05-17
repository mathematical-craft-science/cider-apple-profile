package org.craft.mcs.cap.core.production.domain.model;

import java.math.BigDecimal;
import org.craft.mcs.cap.core.feedstock.domain.model.value.CiderMakerId;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchKind;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchStatus;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate root for a committed production lot created from one or more source batches.
 *
 * <p>The aggregate captures the production intent after a composition/blending decision has been
 * accepted: source composition, product kind, planned status and a chemical profile snapshot at
 * creation time. The snapshot is stored because later feedstock measurements may change, while a
 * production batch must preserve the profile that was used when stock volume was consumed.
 */
@AggregateRoot
public final class ProductionBatch {

  @Identity
  private final @NonNull ProductionBatchId batchId;

  private final @NonNull CiderMakerId ciderMakerId;

  private @NonNull ProductionBatchName name;

  private @NonNull ProductionBatchKind kind;

  private @NonNull ProductionBatchStatus status;

  private @NonNull ProductionSourceComposition sourceComposition;

  private @NonNull ProductionProfileSnapshot profile;

  private @Nullable BigDecimal targetDistance;

  private @Nullable String note;

  private ProductionBatch(
      @NonNull ProductionBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionBatchName name,
      @NonNull ProductionBatchKind kind,
      @NonNull ProductionBatchStatus status,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull ProductionProfileSnapshot profile,
      @Nullable BigDecimal targetDistance,
      @Nullable String note
  ) {
    this.batchId = requireNonNull(batchId, "production batch id cannot be null");
    this.ciderMakerId = requireNonNull(ciderMakerId, "cider maker id cannot be null");
    this.name = requireNonNull(name, "production batch name cannot be null");
    this.kind = requireNonNull(kind, "production batch kind cannot be null");
    this.status = requireNonNull(status, "production batch status cannot be null");
    this.sourceComposition = requireNonNull(sourceComposition, "production source composition cannot be null");
    this.profile = requireNonNull(profile, "production profile cannot be null");
    this.targetDistance = targetDistance;
    this.note = normalize(note);
  }

  public static @NonNull ProductionBatch create(
      @NonNull ProductionBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionBatchName name,
      @NonNull ProductionBatchKind kind,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull ProductionProfileSnapshot profile,
      @Nullable BigDecimal targetDistance,
      @Nullable String note
  ) {
    return new ProductionBatch(
        batchId,
        ciderMakerId,
        name,
        kind,
        ProductionBatchStatus.PLANNED,
        sourceComposition,
        profile,
        targetDistance,
        note
    );
  }

  public static @NonNull ProductionBatch restore(
      @NonNull ProductionBatchId batchId,
      @NonNull CiderMakerId ciderMakerId,
      @NonNull ProductionBatchName name,
      @NonNull ProductionBatchKind kind,
      @NonNull ProductionBatchStatus status,
      @NonNull ProductionSourceComposition sourceComposition,
      @NonNull ProductionProfileSnapshot profile,
      @Nullable BigDecimal targetDistance,
      @Nullable String note
  ) {
    return new ProductionBatch(
        batchId,
        ciderMakerId,
        name,
        kind,
        status,
        sourceComposition,
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

  public @NonNull ProductionSourceComposition sourceComposition() {
    return sourceComposition;
  }

  public @NonNull ProductionProfileSnapshot profile() {
    return profile;
  }

  public @Nullable BigDecimal targetDistance() {
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
