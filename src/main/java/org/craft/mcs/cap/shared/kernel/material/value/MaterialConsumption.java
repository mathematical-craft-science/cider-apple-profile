package org.craft.mcs.cap.shared.kernel.material.value;

import org.craft.mcs.cap.shared.kernel.DomainRuleViolationException;
import org.craft.mcs.cap.shared.kernel.identity.MaterialStateId;
import org.craft.mcs.cap.shared.kernel.measurement.value.VolumeValue;
import org.jmolecules.ddd.annotation.ValueObject;
import org.jspecify.annotations.NonNull;

import static java.util.Objects.requireNonNull;


/**
 * Immutable physical fact describing material consumed from one batch state.
 *
 * <p>The fact enforces volume conservation and identifies the state before consumption and the
 * state of the material remaining in the source batch afterwards.</p>
 */
@ValueObject
public record MaterialConsumption(
    @NonNull VolumeValue availableBefore,
    @NonNull VolumeValue consumed,
    @NonNull VolumeValue remaining,
    @NonNull MaterialStateId sourceStateId,
    @NonNull MaterialStateId remainingStateId
) {

  public MaterialConsumption {
    requireNonNull(availableBefore, "available material volume before consumption cannot be null");
    requireNonNull(consumed, "consumed material volume cannot be null");
    requireNonNull(remaining, "remaining material volume cannot be null");
    requireNonNull(sourceStateId, "source material state id cannot be null");
    requireNonNull(remainingStateId, "remaining material state id cannot be null");
    if (consumed.isZero()) {
      throw new DomainRuleViolationException("consumed material volume must be positive");
    }
    if (sourceStateId.equals(remainingStateId)) {
      throw new DomainRuleViolationException("material consumption must produce a new source state");
    }
    if (availableBefore.liters().compareTo(consumed.liters().add(remaining.liters())) != 0) {
      throw new DomainRuleViolationException(
          "available material volume before consumption must equal consumed plus remaining volume"
      );
    }
  }
}
