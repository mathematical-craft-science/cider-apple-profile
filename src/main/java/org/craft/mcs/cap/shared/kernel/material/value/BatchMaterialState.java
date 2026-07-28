package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Immutable physical state shared by material-owning batch aggregates.
 *
 * <p>The value object keeps lifecycle, remaining volume and state identity consistent. Feedstock
 * and Production compose it while retaining their own aggregate behavior and process rules.
 */
@ValueObject
public record BatchMaterialState(
    @NonNull BatchLifecycle lifecycle,
    @NonNull VolumeValue volume,
    @NonNull MaterialStateId stateId
) {

  public BatchMaterialState {
    requireNonNull(lifecycle, "batch lifecycle cannot be null");
    requireNonNull(volume, "batch material volume cannot be null");
    requireNonNull(stateId, "material state id cannot be null");
    validateLifecycleVolume(lifecycle, volume);
  }

  /**
   * Creates the initial state of a batch that contains available material.
   */
  public static @NonNull BatchMaterialState active(@NonNull VolumeValue volume) {
    return new BatchMaterialState(BatchLifecycle.ACTIVE, volume, MaterialStateId.newId());
  }

  /**
   * Restores a previously persisted material state without changing its identity.
   */
  public static @NonNull BatchMaterialState restore(
      @NonNull BatchLifecycle lifecycle,
      @NonNull VolumeValue volume,
      @NonNull MaterialStateId stateId
  ) {
    return new BatchMaterialState(lifecycle, volume, stateId);
  }

  /**
   * Creates a new state identity when a process transition keeps lifecycle and volume unchanged.
   */
  public @NonNull BatchMaterialState advance() {
    requireActive("advance material state");
    return new BatchMaterialState(lifecycle, volume, MaterialStateId.newId());
  }

  /**
   * Consumes material and marks the new state depleted when no volume remains.
   */
  public @NonNull BatchMaterialState consume(@NonNull VolumeValue consumedVolume) {
    requireActive("consume material");
    final var consumed = requireNonNull(consumedVolume, "consumed material volume cannot be null");
    requirePositive(consumed, "consumed material volume must be positive");
    if (consumed.exceeds(volume)) {
      throw new DomainConflictException("consumed material volume exceeds available volume");
    }

    final var remainingVolume = volume.subtract(consumed);
    final var resultingLifecycle = remainingVolume.isZero()
        ? BatchLifecycle.DEPLETED
        : BatchLifecycle.ACTIVE;
    return new BatchMaterialState(resultingLifecycle, remainingVolume, MaterialStateId.newId());
  }

  /**
   * Records a smaller positive remaining volume after a loss-producing operation.
   */
  public @NonNull BatchMaterialState reduceTo(@NonNull VolumeValue remainingVolume) {
    requireActive("reduce material volume");
    final var remaining = requireNonNull(remainingVolume, "remaining material volume cannot be null");
    requirePositive(remaining, "remaining material volume must be greater than zero");
    if (remaining.exceeds(volume)) {
      throw new DomainConflictException("remaining material volume cannot exceed current volume");
    }
    if (remaining.equals(volume)) {
      throw new DomainConflictException("remaining material volume must be less than current volume");
    }
    return new BatchMaterialState(BatchLifecycle.ACTIVE, remaining, MaterialStateId.newId());
  }

  /**
   * Marks that all material has left through confirmed downstream operations.
   */
  public @NonNull BatchMaterialState deplete() {
    requireActive("deplete material");
    return terminal(BatchLifecycle.DEPLETED);
  }

  /**
   * Marks that the remaining material was intentionally removed from use.
   */
  public @NonNull BatchMaterialState discard() {
    requireActive("discard material");
    return terminal(BatchLifecycle.DISCARDED);
  }

  private @NonNull BatchMaterialState terminal(@NonNull BatchLifecycle terminalLifecycle) {
    return new BatchMaterialState(
        terminalLifecycle,
        VolumeValue.ofLiters("0"),
        MaterialStateId.newId()
    );
  }

  private void requireActive(@NonNull String action) {
    if (lifecycle != BatchLifecycle.ACTIVE) {
      throw new DomainConflictException("cannot " + action + " when batch lifecycle is " + lifecycle);
    }
  }

  private static void validateLifecycleVolume(
      @NonNull BatchLifecycle lifecycle,
      @NonNull VolumeValue volume
  ) {
    if (lifecycle == BatchLifecycle.ACTIVE && volume.isZero()) {
      throw new DomainRuleViolationException("active batch material volume must be greater than zero");
    }
    if (lifecycle != BatchLifecycle.ACTIVE && !volume.isZero()) {
      throw new DomainRuleViolationException("inactive batch material volume must be zero");
    }
  }

  private static void requirePositive(
      @NonNull VolumeValue volume,
      @NonNull String message
  ) {
    if (volume.isZero()) {
      throw new DomainRuleViolationException(message);
    }
  }
}
