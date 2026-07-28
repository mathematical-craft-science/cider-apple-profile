package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/** Immutable availability fact for material selected from one current batch state. */
@ValueObject
public record MaterialAvailability(
    @NonNull VolumeValue requestedVolume,
    @NonNull VolumeValue availableVolume,
    @NonNull MaterialStateId materialStateId
) {

  public MaterialAvailability {
    requireNonNull(requestedVolume, "requested material volume cannot be null");
    requireNonNull(availableVolume, "available material volume cannot be null");
    requireNonNull(materialStateId, "available material state id cannot be null");
    if (requestedVolume.isZero()) {
      throw new DomainRuleViolationException("requested material volume must be positive");
    }
    if (availableVolume.isZero()) {
      throw new DomainRuleViolationException("available material volume must be positive");
    }
    if (requestedVolume.exceeds(availableVolume)) {
      throw new DomainRuleViolationException("requested material volume cannot exceed available volume");
    }
  }
}
