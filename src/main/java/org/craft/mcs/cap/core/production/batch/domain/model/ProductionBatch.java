package org.craft.mcs.cap.core.production.batch.domain.model;

import org.craft.mcs.cap.core.production.batch.domain.model.value.AlcoholicFermentationState;
import org.craft.mcs.cap.shared.kernel.material.value.BatchName;
import org.craft.mcs.cap.core.production.batch.domain.model.value.ProductionProfileSnapshot;
import org.craft.mcs.cap.shared.kernel.material.value.BatchLifecycle;
import org.craft.mcs.cap.shared.kernel.material.value.BatchMaterialState;
import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
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

  private @NonNull VesselId vesselId;

  private @NonNull AlcoholicFermentationState alcoholicFermentationState;

  private @NonNull BatchMaterialState materialState;

  private final @NonNull ProductionProfileSnapshot initialProfileSnapshot;

  private @Nullable String note;

  @SuppressWarnings("java:S107")
  private ProductionBatch(
      @NonNull ProductionBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @NonNull VesselId vesselId,
      @NonNull AlcoholicFermentationState alcoholicFermentationState,
      @NonNull BatchMaterialState materialState,
      @NonNull ProductionProfileSnapshot initialProfileSnapshot,
      @Nullable String note
  ) {
    this.batchId = requireNonNull(batchId, "production batch id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.name = requireNonNull(name, "production batch name cannot be null");
    this.vesselId = requireNonNull(vesselId, "production vessel id cannot be null");
    this.alcoholicFermentationState = requireNonNull(
        alcoholicFermentationState,
        "alcoholic fermentation state cannot be null"
    );
    this.materialState = requireNonNull(materialState, "production material state cannot be null");
    this.initialProfileSnapshot = requireNonNull(
        initialProfileSnapshot,
        "production initial profile snapshot cannot be null"
    );
    this.note = normalize(note);
  }

  public static @NonNull ProductionBatch create(
      @NonNull ProductionBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @NonNull VesselId vesselId,
      @NonNull VolumeValue initialVolume,
      @NonNull ProductionProfileSnapshot initialProfileSnapshot,
      @Nullable String note
  ) {
    return new ProductionBatch(
        batchId,
        workspaceId,
        name,
        vesselId,
        AlcoholicFermentationState.NOT_STARTED,
        BatchMaterialState.active(initialVolume),
        initialProfileSnapshot,
        note
    );
  }

  @SuppressWarnings("java:S107")
  public static @NonNull ProductionBatch restore(
      @NonNull ProductionBatchId batchId,
      @NonNull WorkspaceId workspaceId,
      @NonNull BatchName name,
      @NonNull VesselId vesselId,
      @NonNull BatchLifecycle lifecycle,
      @NonNull AlcoholicFermentationState alcoholicFermentationState,
      @NonNull VolumeValue currentVolume,
      @NonNull MaterialStateId currentMaterialStateId,
      @NonNull ProductionProfileSnapshot initialProfileSnapshot,
      @Nullable String note
  ) {
    return new ProductionBatch(
        batchId,
        workspaceId,
        name,
        vesselId,
        alcoholicFermentationState,
        BatchMaterialState.restore(lifecycle, currentVolume, currentMaterialStateId),
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

  public @NonNull VesselId vesselId() {
    return vesselId;
  }

  public @NonNull BatchLifecycle lifecycle() {
    return materialState.lifecycle();
  }

  public @NonNull AlcoholicFermentationState alcoholicFermentationState() {
    return alcoholicFermentationState;
  }

  public @NonNull VolumeValue currentVolume() {
    return materialState.volume();
  }

  public @NonNull MaterialStateId currentMaterialStateId() {
    return materialState.stateId();
  }

  public @NonNull ProductionProfileSnapshot initialProfileSnapshot() {
    return initialProfileSnapshot;
  }

  public @Nullable String note() {
    return note;
  }

  /** Ensures that measurement evidence may still be added, corrected, or retracted for this batch. */
  public void requireMeasurementChangesAllowed() {
    if (lifecycle() != BatchLifecycle.ACTIVE) {
      throw new DomainConflictException(
          "production batch measurements can be changed only when lifecycle is ACTIVE"
      );
    }
  }

  public @NonNull ProductionBatch rename(@NonNull BatchName name) {
    this.name = requireNonNull(name, "production batch name cannot be null");
    return this;
  }

  /** Corrects the initial vessel assignment before any operation has been recorded for this batch. */
  public @NonNull ProductionBatch correctInitialVesselAssignment(@NonNull VesselId vesselId) {
    this.vesselId = requireNonNull(vesselId, "production vessel id cannot be null");
    return this;
  }

  public @NonNull ProductionBatch changeNote(@Nullable String note) {
    this.note = normalize(note);
    return this;
  }

  /**
   * Records a full transfer of the active batch to another vessel.
   */
  public @NonNull ProductionBatch transferTo(@NonNull VesselId vesselId) {
    requireActive("transfer");
    final var targetVesselId = requireNonNull(vesselId, "production vessel id cannot be null");
    if (this.vesselId.equals(targetVesselId)) {
      throw new DomainConflictException("target production vessel must differ from current vessel");
    }
    this.vesselId = targetVesselId;
    materialState = materialState.advance();
    return this;
  }

  /**
   * Records the material remaining after a loss-producing operation while keeping the batch active.
   */
  public @NonNull ProductionBatch reduceCurrentVolumeTo(@NonNull VolumeValue remainingVolume) {
    requireActive("reduce volume");
    materialState = materialState.reduceTo(remainingVolume);
    return this;
  }

  /**
   * Marks that all material has left the batch through valid downstream operations.
   */
  public @NonNull ProductionBatch deplete() {
    materialState = materialState.deplete();
    return this;
  }

  /**
   * Marks that the remaining material was intentionally removed from production.
   */
  public @NonNull ProductionBatch discard() {
    materialState = materialState.discard();
    return this;
  }

  /**
   * Confirms that the primary alcoholic fermentation has started.
   */
  public @NonNull ProductionBatch startAlcoholicFermentation() {
    requireActive("start alcoholic fermentation");
    requireFermentationState(AlcoholicFermentationState.NOT_STARTED, "start alcoholic fermentation");
    alcoholicFermentationState = AlcoholicFermentationState.ACTIVE;
    materialState = materialState.advance();
    return this;
  }

  /**
   * Records an unplanned stop that may still be resumed.
   */
  public @NonNull ProductionBatch arrestAlcoholicFermentation() {
    requireActive("arrest alcoholic fermentation");
    requireFermentationState(AlcoholicFermentationState.ACTIVE, "arrest alcoholic fermentation");
    alcoholicFermentationState = AlcoholicFermentationState.ARRESTED;
    materialState = materialState.advance();
    return this;
  }

  /**
   * Confirms that an arrested primary alcoholic fermentation has resumed.
   */
  public @NonNull ProductionBatch resumeAlcoholicFermentation() {
    requireActive("resume alcoholic fermentation");
    requireFermentationState(AlcoholicFermentationState.ARRESTED, "resume alcoholic fermentation");
    alcoholicFermentationState = AlcoholicFermentationState.ACTIVE;
    materialState = materialState.advance();
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
    materialState = materialState.advance();
    return this;
  }

  private void requireActive(@NonNull String action) {
    if (materialState.lifecycle() != BatchLifecycle.ACTIVE) {
      throw new DomainConflictException(
          "cannot " + action + " when production batch lifecycle is " + materialState.lifecycle()
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

  private static @Nullable String normalize(@Nullable String value) {
    if (value == null) {
      return null;
    }

    final var normalized = value.strip();
    return normalized.isEmpty() ? null : normalized;
  }
}
