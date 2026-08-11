package org.craft.mcs.cap.core.vessel.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainConflictException;
import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.VesselId;
import org.craft.mcs.cap.shared.kernel.identity.WorkspaceId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Aggregate root representing a physical vessel registered in one workspace.
 *
 * <p>The aggregate owns stable identity, catalog characteristics and operational availability.
 * Material occupancy remains owned by material batches and their operations, preventing Vessel and
 * Batch from competing over the same state.</p>
 */
@AggregateRoot
public final class Vessel {

  private static final int MAX_NAME_LENGTH = 160;

  @Identity
  private final @NonNull VesselId vesselId;

  private final @NonNull WorkspaceId workspaceId;

  private @NonNull String name;

  private @NonNull VolumeValue nominalCapacity;

  private @NonNull VesselMaterial material;

  private @NonNull VesselOperationalStatus operationalStatus;

  private Vessel(
      @NonNull VesselId vesselId,
      @NonNull WorkspaceId workspaceId,
      @NonNull String name,
      @NonNull VolumeValue nominalCapacity,
      @NonNull VesselMaterial material,
      @NonNull VesselOperationalStatus operationalStatus
  ) {
    this.vesselId = requireNonNull(vesselId, "vessel id cannot be null");
    this.workspaceId = requireNonNull(workspaceId, "workspace id cannot be null");
    this.name = validName(name);
    this.nominalCapacity = positiveCapacity(nominalCapacity);
    this.material = requireNonNull(material, "vessel material cannot be null");
    this.operationalStatus = requireNonNull(operationalStatus, "vessel operational status cannot be null");
  }

  public static @NonNull Vessel create(
      @NonNull VesselId vesselId,
      @NonNull WorkspaceId workspaceId,
      @NonNull String name,
      @NonNull VolumeValue nominalCapacity,
      @NonNull VesselMaterial material
  ) {
    return new Vessel(
        vesselId,
        workspaceId,
        name,
        nominalCapacity,
        material,
        VesselOperationalStatus.OPERATIONAL
    );
  }

  public static @NonNull Vessel restore(
      @NonNull VesselId vesselId,
      @NonNull WorkspaceId workspaceId,
      @NonNull String name,
      @NonNull VolumeValue nominalCapacity,
      @NonNull VesselMaterial material,
      @NonNull VesselOperationalStatus operationalStatus
  ) {
    return new Vessel(vesselId, workspaceId, name, nominalCapacity, material, operationalStatus);
  }

  public @NonNull VesselId vesselId() {
    return vesselId;
  }

  public @NonNull WorkspaceId workspaceId() {
    return workspaceId;
  }

  public @NonNull String name() {
    return name;
  }

  public @NonNull VolumeValue nominalCapacity() {
    return nominalCapacity;
  }

  public @NonNull VesselMaterial material() {
    return material;
  }

  public @NonNull VesselOperationalStatus operationalStatus() {
    return operationalStatus;
  }

  /** Updates editable catalog characteristics while preserving vessel identity. */
  public @NonNull Vessel updateCharacteristics(
      @NonNull String name,
      @NonNull VolumeValue nominalCapacity,
      @NonNull VesselMaterial material
  ) {
    requireNotRetired("update vessel characteristics");
    this.name = validName(name);
    this.nominalCapacity = positiveCapacity(nominalCapacity);
    this.material = requireNonNull(material, "vessel material cannot be null");
    return this;
  }

  /** Changes temporary or terminal operational availability. */
  public @NonNull Vessel changeOperationalStatus(@NonNull VesselOperationalStatus status) {
    final var targetStatus = requireNonNull(status, "vessel operational status cannot be null");
    if (operationalStatus == targetStatus) {
      return this;
    }
    requireNotRetired("change vessel operational status");
    operationalStatus = targetStatus;
    return this;
  }

  /** Verifies that the vessel is operational and has capacity for newly assigned material. */
  public void requireCanReceive(@NonNull VolumeValue volume) {
    if (operationalStatus != VesselOperationalStatus.OPERATIONAL) {
      throw new DomainConflictException("vessel is not operational");
    }
    requireCapacityFor(volume);
  }

  /** Verifies nominal capacity against material already occupying the vessel. */
  public void requireCapacityFor(@NonNull VolumeValue volume) {
    final var requestedVolume = positiveMaterialVolume(volume);
    if (requestedVolume.exceeds(nominalCapacity)) {
      throw new DomainConflictException("material volume exceeds vessel nominal capacity");
    }
  }

  private void requireNotRetired(@NonNull String action) {
    if (operationalStatus == VesselOperationalStatus.RETIRED) {
      throw new DomainConflictException("cannot " + action + " after vessel retirement");
    }
  }

  private static @NonNull String validName(@NonNull String name) {
    final var normalized = requireNonNull(name, "vessel name cannot be null").strip();
    if (normalized.isBlank()) {
      throw new DomainRuleViolationException("vessel name cannot be blank");
    }
    if (normalized.length() > MAX_NAME_LENGTH) {
      throw new DomainRuleViolationException(
          "vessel name length must not exceed " + MAX_NAME_LENGTH + " characters"
      );
    }
    return normalized;
  }

  private static @NonNull VolumeValue positiveCapacity(@NonNull VolumeValue capacity) {
    final var value = requireNonNull(capacity, "vessel nominal capacity cannot be null");
    if (value.isZero()) {
      throw new DomainRuleViolationException("vessel nominal capacity must be greater than zero");
    }
    return value;
  }

  private static @NonNull VolumeValue positiveMaterialVolume(@NonNull VolumeValue volume) {
    final var value = requireNonNull(volume, "material volume cannot be null");
    if (value.isZero()) {
      throw new DomainRuleViolationException("material volume must be greater than zero");
    }
    return value;
  }
}
