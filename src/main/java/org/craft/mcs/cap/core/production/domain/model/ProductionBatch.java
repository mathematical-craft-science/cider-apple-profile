package org.craft.mcs.cap.core.production.domain.model;

import org.craft.mcs.cap.core.production.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionBatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.material.value.Vessel;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate root for a confirmed physical production lot.
 *
 * <p>The aggregate owns the material lifecycle, current vessel and volume, and the state of the
 * primary alcoholic fermentation cycle. Its profile is the immutable snapshot accepted at
 * creation. Current measured characteristics, operations and source lineage belong to their
 * respective contexts and are projected alongside this state on the read side.
 */
@AggregateRoot
public final class ProductionBatch {

  @Identity
  private final @NonNull ProductionBatchId batchId;

  private final @NonNull WorkspaceId workspaceId;

  private @NonNull BatchName name;

  private @NonNull Vessel vessel;

  private @NonNull ProductionBatchLifecycle lifecycle;

  private @NonNull AlcoholicFermentationState alcoholicFermentationState;

  private @NonNull VolumeValue currentVolume;

  private final @NonNull ProductionProfileSnapshot initialProfileSnapshot;

  private @Nullable String note;

  @SuppressWarnings("java:S107")
  private ProductionBatch(
      @NonNull ProductionBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @NonNull Vessel vessel,
      @NonNull ProductionBatchLifecycle lifecycle,
      @NonNull AlcoholicFermentationState alcoholicFermentationState,
      @NonNull VolumeValue currentVolume,
      @NonNull ProductionProfileSnapshot initialProfileSnapshot,
      @Nullable String note
  ) {
    this.batchId = requireNonNull(batchId, "production batch id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.name = requireNonNull(name, "production batch name cannot be null");
    this.vessel = requireNonNull(vessel, "production vessel cannot be null");
    this.lifecycle = requireNonNull(lifecycle, "production batch lifecycle cannot be null");
    this.alcoholicFermentationState = requireNonNull(
        alcoholicFermentationState,
        "alcoholic fermentation state cannot be null"
    );
    this.currentVolume = requireNonNull(currentVolume, "production current volume cannot be null");
    this.initialProfileSnapshot = requireNonNull(
        initialProfileSnapshot,
        "production initial profile snapshot cannot be null"
    );
    this.note = normalize(note);
    validateMaterialState(lifecycle, currentVolume);
  }

  public static @NonNull ProductionBatch create(
      @NonNull ProductionBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @NonNull Vessel vessel,
      @NonNull VolumeValue initialVolume,
      @NonNull ProductionProfileSnapshot initialProfileSnapshot,
      @Nullable String note
  ) {
    requirePositiveVolume(initialVolume);
    return new ProductionBatch(
        batchId,
        workspaceId,
        name,
        vessel,
        ProductionBatchLifecycle.ACTIVE,
        AlcoholicFermentationState.NOT_STARTED,
        initialVolume,
        initialProfileSnapshot,
        note
    );
  }

  @SuppressWarnings("java:S107")
  public static @NonNull ProductionBatch restore(
      @NonNull ProductionBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @NonNull Vessel vessel,
      @NonNull ProductionBatchLifecycle lifecycle,
      @NonNull AlcoholicFermentationState alcoholicFermentationState,
      @NonNull VolumeValue currentVolume,
      @NonNull ProductionProfileSnapshot initialProfileSnapshot,
      @Nullable String note
  ) {
    return new ProductionBatch(
        batchId,
        workspaceId,
        name,
        vessel,
        lifecycle,
        alcoholicFermentationState,
        currentVolume,
        initialProfileSnapshot,
        note
    );
  }

  public @NonNull ProductionBatchId batchId() {
    return batchId;
  }

  public @NonNull WorkspaceId workspaceId() {
    return workspaceId;
  }

  public @NonNull BatchName name() {
    return name;
  }

  public @NonNull Vessel vessel() {
    return vessel;
  }

  public @NonNull ProductionBatchLifecycle lifecycle() {
    return lifecycle;
  }

  public @NonNull AlcoholicFermentationState alcoholicFermentationState() {
    return alcoholicFermentationState;
  }

  public @NonNull VolumeValue currentVolume() {
    return currentVolume;
  }

  public @NonNull ProductionProfileSnapshot initialProfileSnapshot() {
    return initialProfileSnapshot;
  }

  public @Nullable String note() {
    return note;
  }

  /**
   * Records a full transfer of the active batch to another vessel.
   */
  public @NonNull ProductionBatch transferTo(@NonNull Vessel vessel) {
    requireActive("transfer");
    this.vessel = requireNonNull(vessel, "production vessel cannot be null");
    return this;
  }

  /**
   * Records the material remaining after a loss-producing operation while keeping the batch active.
   */
  public @NonNull ProductionBatch reduceCurrentVolumeTo(@NonNull VolumeValue remainingVolume) {
    requireActive("reduce volume");
    requirePositiveVolume(remainingVolume);
    if (remainingVolume.exceeds(currentVolume)) {
      throw new DomainConflictException("remaining production volume cannot exceed current volume");
    }
    currentVolume = remainingVolume;
    return this;
  }

  /**
   * Marks that all material has left the batch through valid downstream operations.
   */
  public @NonNull ProductionBatch deplete() {
    requireActive("deplete");
    lifecycle = ProductionBatchLifecycle.DEPLETED;
    currentVolume = VolumeValue.ofLiters("0");
    return this;
  }

  /**
   * Marks that the remaining material was intentionally removed from production.
   */
  public @NonNull ProductionBatch discard() {
    requireActive("discard");
    lifecycle = ProductionBatchLifecycle.DISCARDED;
    currentVolume = VolumeValue.ofLiters("0");
    return this;
  }

  /**
   * Confirms that the primary alcoholic fermentation has started.
   */
  public @NonNull ProductionBatch startAlcoholicFermentation() {
    requireActive("start alcoholic fermentation");
    requireFermentationState(AlcoholicFermentationState.NOT_STARTED, "start alcoholic fermentation");
    alcoholicFermentationState = AlcoholicFermentationState.ACTIVE;
    return this;
  }

  /**
   * Records an unplanned stop that may still be resumed.
   */
  public @NonNull ProductionBatch arrestAlcoholicFermentation() {
    requireActive("arrest alcoholic fermentation");
    requireFermentationState(AlcoholicFermentationState.ACTIVE, "arrest alcoholic fermentation");
    alcoholicFermentationState = AlcoholicFermentationState.ARRESTED;
    return this;
  }

  /**
   * Confirms that an arrested primary alcoholic fermentation has resumed.
   */
  public @NonNull ProductionBatch resumeAlcoholicFermentation() {
    requireActive("resume alcoholic fermentation");
    requireFermentationState(AlcoholicFermentationState.ARRESTED, "resume alcoholic fermentation");
    alcoholicFermentationState = AlcoholicFermentationState.ACTIVE;
    return this;
  }

  /**
   * Confirms completion of the primary alcoholic fermentation cycle.
   */
  public @NonNull ProductionBatch completeAlcoholicFermentation() {
    requireActive("complete alcoholic fermentation");
    if (alcoholicFermentationState != AlcoholicFermentationState.ACTIVE
        && alcoholicFermentationState != AlcoholicFermentationState.ARRESTED
    ) {
      throw invalidFermentationTransition("complete alcoholic fermentation");
    }
    alcoholicFermentationState = AlcoholicFermentationState.COMPLETED;
    return this;
  }

  private void requireActive(@NonNull String action) {
    if (lifecycle != ProductionBatchLifecycle.ACTIVE) {
      throw new DomainConflictException(
          "cannot " + action + " when production batch lifecycle is " + lifecycle
      );
    }
  }

  private void requireFermentationState(
      @NonNull AlcoholicFermentationState expected,
      @NonNull String action
  ) {
    if (alcoholicFermentationState != expected) {
      throw invalidFermentationTransition(action);
    }
  }

  private @NonNull DomainConflictException invalidFermentationTransition(@NonNull String action) {
    return new DomainConflictException(
        "cannot " + action + " when alcoholic fermentation state is " + alcoholicFermentationState
    );
  }

  private static void validateMaterialState(
      @NonNull ProductionBatchLifecycle lifecycle,
      @NonNull VolumeValue currentVolume
  ) {
    if (lifecycle == ProductionBatchLifecycle.ACTIVE && currentVolume.isZero()) {
      throw new DomainRuleViolationException("active production batch volume must be greater than zero");
    }
    if (lifecycle != ProductionBatchLifecycle.ACTIVE && !currentVolume.isZero()) {
      throw new DomainRuleViolationException("inactive production batch volume must be zero");
    }
  }

  private static void requirePositiveVolume(@NonNull VolumeValue volume) {
    requireNonNull(volume, "production volume cannot be null");
    if (volume.isZero()) {
      throw new DomainRuleViolationException("production batch volume must be greater than zero");
    }
  }

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
