package org.craft.mcs.cap.core.production.domain.model;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;

import org.craft.mcs.cap.shared.kernel.measurement.VolumeValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


@ValueObject
public record ProductionSourceComponent(
    @NonNull ProductionSourceReference reference,
    @NonNull VolumeValue volume
) {

  public ProductionSourceComponent {
    requireNonNull(reference, "production source reference cannot be null");
    requireNonNull(volume, "production source volume cannot be null");
    if (volume.isZero()) {
      throw new DomainRuleViolationException("production source volume must be positive");
    }
  }
}
